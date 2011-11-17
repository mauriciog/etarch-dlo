#include <jni.h>  
#include "br_ufu_facom_network_dlontology_FinSocket.h"  

#include <net/if.h>
#include <netinet/ether.h>
#include <sys/ioctl.h>
#include <stdio.h>
#include <string.h>
#include <malloc.h>
#include <sys/socket.h>
#include <net/ethernet.h>
#include <linux/if_packet.h>
#include <math.h>

#include <errno.h>

/*
 * Class:     FinSocket
 * Method:    finOpen
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_br_ufu_facom_network_dlontology_FinSocket_finOpen
  (JNIEnv * env , jobject obj){
  int s;

  s = socket(AF_PACKET, SOCK_RAW, htons(ETH_P_ALL));
  if (s == -1)
  	printf("socket error\n");

  return s;
}

/*
 * Class:     FinSocket
 * Method:    finClose
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_br_ufu_facom_network_dlontology_FinSocket_finClose
  (JNIEnv *env, jobject obj, jint sock){

  return close(sock);
}

/*
 * Class:     FinSocket
 * Method:    finWrite
 * Signature: (Ljava/lang/String;Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_br_ufu_facom_network_dlontology_FinSocket_finWrite
  (JNIEnv * env, jobject obj, jint soc, jbyteArray data, jint offset, jint len){
	int result;
	jbyte *buf;

        /*target address*/
        struct sockaddr_ll socket_address;

        /*we don't use a protocoll above ethernet layer
          ->just use anything here*/
        socket_address.sll_protocol = htons(ETH_P_ALL);

        /*index of the network device
        see full code later how to retrieve it*/
        socket_address.sll_ifindex  = 2;

	//Packet Broadcast
	//socket_address.sll_pkttype = PACKET_BROADCAST;

	//Tamanho do endereço
	socket_address.sll_halen = ETH_ALEN;

        buf = (*env)->GetByteArrayElements(env, data, NULL);

	if(finSelect(soc,0,2,0)<0)
		return 1;

  	result = sendto(soc, buf+offset, len, 0,(struct sockaddr*)&socket_address, sizeof(socket_address));

  	(*env)->ReleaseByteArrayElements(env, data, buf, JNI_ABORT);

	return result > 0;
	
}

JNIEXPORT jint JNICALL Java_br_ufu_facom_network_dlontology_FinSocket_finRead
  (JNIEnv * env, jobject obj, jint soc, jbyteArray data, jint offset, jint len){
	
	jbyte *buf;
	int result;

	buf = (*env)->GetByteArrayElements(env, data, NULL);

	//result = recvfrom(soc, buf+offset, len, 0, NULL, NULL);
	result = recv(soc, buf+offset, len, 0);

  	(*env)->ReleaseByteArrayElements(env, data, buf, 0);

        return result;
}

JNIEXPORT jboolean JNICALL Java_br_ufu_facom_network_dlontology_FinSocket_setPromiscousMode
  (JNIEnv * env, jobject obj, jint soc){
        struct ifreq ifr;

        // O procedimento abaixo é utilizado para "setar" a 
        // interface em modo promíscuo
        strcpy(ifr.ifr_name, "eth0");
        if(ioctl(soc, SIOCGIFINDEX, &ifr) < 0) return 0;
	if(ioctl(soc, SIOCGIFFLAGS, &ifr) < 0) return 0;
        ifr.ifr_flags |= IFF_PROMISC;
        if(ioctl(soc, SIOCSIFFLAGS, &ifr) < 0) return 0;

	return 1;
}

int finSelect(int socket, int read, int seconds, int microseconds){
  int result;
  struct timeval timeout;
  fd_set *rset = NULL, *wset = NULL, errset, fdset;

  FD_ZERO(&fdset);
  FD_ZERO(&errset);
  FD_SET(socket, &fdset);
  FD_SET(socket, &errset);

  timeout.tv_sec  = seconds;
  timeout.tv_usec = microseconds;

  if(read){
	printf("Read! =O\n");
    rset = &fdset;
  }else
    wset = &fdset;

  result = select(socket + 1, rset, wset, &errset, &timeout);

  if(result >= 0) {
    if(FD_ISSET(socket, &errset))
      result = -1;
    else if(FD_ISSET(socket, &fdset))
      result = 0;
    else {
      result = -1;
    }
  }

  return result;
}

