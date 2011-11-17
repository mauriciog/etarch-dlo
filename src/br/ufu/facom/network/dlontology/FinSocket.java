package br.ufu.facom.network.dlontology;
import java.util.ArrayList;
import java.util.List;

import sun.java2d.loops.CustomComponent;

import br.ufu.facom.network.dlontology.msg.CustomParser;
import br.ufu.facom.network.dlontology.msg.Message;
import br.ufu.facom.network.dlontology.msg.OWLParser;

public class FinSocket {
	
	//Carrega a biblioteca libFinSocket.so
    static {  
    	System.loadLibrary("FinSocket");  
    }

	//Constants
	private static final int MAX_FRAME_SIZE=1500;

	//Functional
	private int sock;
	private boolean promisc;

	//Non-Function
	private boolean registered;
	private String title;
	private List<String> workspaces;

	//Parser
	private static OWLParser parser = new CustomParser();
	
	private FinSocket(){
		this.sock = finOpen();
		this.workspaces = new ArrayList<String>();
	}

	//Métodos nativos
	private native int finOpen();

	private native boolean finClose(int sock);

	private native boolean finWrite(int sock, byte[] data, int offset, int len);

	private native int finRead(int sock, byte[] data, int offset, int len);

	private native boolean setPromiscousMode(int sock);

    //Interface
	public boolean close(){
		return finClose(sock);
	}

	public static FinSocket open(){
		return new FinSocket();
	}
	
	public boolean write(String destin, String msg){
		return write(new Message(this.title,destin,msg));
	}
	
	public boolean write(Message message){
		String msgOWL = parser.parse(message);
		
		debug("Sending message...");

		byte bytes[] = 	msgOWL.getBytes();

		return finWrite(sock, bytes, 0, bytes.length);
	}


	public Message read(){
		if(!promisc)
			if(!setPromiscousMode(sock)){
				System.err.println("Não foi possível colocar a interface em modo promíscuo.");
				return null;
			}else
				promisc = true;

		
		byte bytes[] = new byte[MAX_FRAME_SIZE];
		Message msgObj = null;
		int offset = 0;
	
		while(true){		
			
			offset += finRead(sock,bytes,offset,MAX_FRAME_SIZE-offset);

			String msg = new String(bytes,0,offset);
		
			if(msg.startsWith("<Message")){
				if(msg.endsWith("</Message>")){ // Message Complete
					msgObj = parser.parseMessage(msg);
					
					if(msgObj != null && workspaces.contains(msgObj.getDestination())){
						debug("Receiving Message... Destin.:"+msgObj.getDestination());
						return msgObj;
					}else{
						msgObj=null;
						offset = 0;
					}
				}
			}else // Não é uma mensagem FinLan
				offset = 0;
		}

	}
	
	//DTS Interaction
	public boolean register(String title){
		String msg = "<Subscriber rdf:about=\"#Register_"+title+"\"><rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/><Entity rdf:resource=\"#"+title+"\"/></Subscriber>";
		
		if(write(new Message("","DTS",msg))){
			this.title = title;
			this.registered = true;
		
			return true;
		}else{
			System.err.println("Não foi possivel enviar mensagem de registro.");
			return false;
		}
	}
	
	public boolean unregister(String title){
		if(registered){
			String msg = "<Unsubscriber rdf:about=\"#Unregister_"+title+"\"><rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/><Entity rdf:resource=\"#"+title+"\"/></Unsubscriber>";
		
			write(new Message("","DTS",msg));
		
			this.title = null;
			this.registered = false;
		
			return true;
		}
		return false;
	}
	
	public boolean join(String workspace){
		String msg = "<Join rdf:about=\"#Join_"+title+"_"+workspace+"\"><rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/><Entity rdf:resource=\"#"+title+"\"/><Workspace rdf:resource=\"#"+workspace+"\"/></Join>";
		
		write("DTS",msg);
		
		this.workspaces.add(workspace);
		
		return true;
	}
	
	public boolean disjoin(String workspace){
		if(this.workspaces.contains(workspace)){
			String msg = "<Disjoin rdf:about=\"#Disjoin_"+title+"_"+workspace+"\"><rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/><Entity rdf:resource=\"#"+title+"\"/><Workspace rdf:resource=\"#"+workspace+"\"/>\n</Disjoin>";
		
			write("DTS",msg);
		
			this.workspaces.add(workspace);
		}
		return true;
	}

	//Function to print the debug
	private void debug(String info) {
		System.out.println(info);
	}
} 
