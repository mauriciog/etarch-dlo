package br.ufu.facom.network.dlontology;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	private int sock = -1;
	private boolean promisc = false;

	//Non-Function
	private boolean registered;
	private String title;
	private List<String> workspaces;

	private int ifIndex = -1;
	private String ifName = null;
	
	//Parser
	private static OWLParser parser = new CustomParser();

	//Métodos nativos
	private native int finOpen();

	private native boolean finClose(int sock);

	private native boolean finWrite(int ifIndex, int sock, byte[] data, int offset, int len);

	private native int finRead(int sock, byte[] data, int offset, int len);

	private native boolean setPromiscousMode(String ifName, int sock);
	
	private native Map<Integer,String> getNetIfs();

    //Interface
	public boolean close(){
		return finClose(sock);
	}

	public boolean open(){
		this.sock = finOpen();
		this.workspaces = new ArrayList<String>();
		
		Map<Integer,String> ifs = getNetIfs();
		for(Integer index : ifs.keySet()){
			String name = ifs.get(index); 
			if(!name.startsWith("lo")){
				ifIndex = index;
				ifName = name; 
			}
		}
		
		return isOpenned();
	}
	
	private boolean isOpenned() {
		return this.sock >= 0 && ifIndex >=0;  
	}

	public boolean write(String destin, byte[] msg){
		return write(new Message(this.title,destin,msg));
	}
	
	public boolean write(Message message){
		if(isOpenned()){
			debug("Sending message... "+message.getLabel());
	
			byte bytes[] = parser.parse(message);
			
			int offset = 0;
			boolean res = false;
			while(offset < bytes.length){
				
				int len = Math.min(bytes.length - offset, MAX_FRAME_SIZE);
				
				res |= finWrite(ifIndex, sock, bytes, offset, len);
				
				offset += len;
			}
			
			return res;
		}else{
			throw new RuntimeException("FinSocket não aberto!");
		}
	}


	public Message read(){
		if(isOpenned()){
			if(!promisc)
				if(!setPromiscousMode(ifName,sock)){
					System.err.println("Não foi possível colocar a interface em modo promíscuo.");
					return null;
				}else
					promisc = true;
	
			
			byte bytes[] = new byte[1500000];
			Message msgObj = null;
			int offset = 0;
		
			while(true){		
				
				offset += finRead(sock,bytes,offset,MAX_FRAME_SIZE);
	
				if(parser.validStartMessage(bytes)){
					if(parser.validEndMessage(bytes,offset)){ // Message Complete
						msgObj = parser.parseMessage(bytes);
						
						if(msgObj != null && workspaces.contains(msgObj.getDestination())){
							debug("Receiving Message... Destin.:"+msgObj.getDestination());
							return msgObj;
						}else{
							if(msgObj == null)
								debug("Parse Fail!");
							else
								debug("Workspace fail : " + msgObj.getDestination());
							msgObj=null;
							offset = 0;
						}
					}else{
						//System.out.println("Ending fail");
					}
				}else{ // Não é uma mensagem FinLan
					debug("Non-Finlan message!");
					offset = 0;
				}
			}
		}else{
			throw new RuntimeException("FinSocket não aberto!");
		}
	}
	
	//DTS Interaction
	public boolean register(String title){
		String msg = "<Subscriber rdf:about=\"#Register_"+title+"\"><rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/><Entity rdf:resource=\"#"+title+"\"/></Subscriber>";
		
		if(write(new Message("","DTS",msg.getBytes()))){
			this.title = title;
			this.registered = true;
		
			return true;
		}else{
			System.err.println("Não foi possivel enviar mensagem de registro.");
			return false;
		}
	}
	
	public boolean unregister(){
		if(registered){
			String msg = "<Unsubscriber rdf:about=\"#Unregister_"+title+"\"><rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/><Entity rdf:resource=\"#"+title+"\"/></Unsubscriber>";
		
			write(new Message("","DTS",msg.getBytes()));
		
			this.title = null;
			this.registered = false;
		
			return true;
		}
		return false;
	}
	
	public boolean join(String workspace){
		String msg = "<Join rdf:about=\"#Join_"+title+"_"+workspace+"\"><rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/><Entity rdf:resource=\"#"+title+"\"/><Workspace rdf:resource=\"#"+workspace+"\"/></Join>";
		
		write("DTS",msg.getBytes());
		
		this.workspaces.add(workspace);
		
		return true;
	}
	
	public boolean disjoin(String workspace){
		if(this.workspaces.contains(workspace)){
			String msg = "<Disjoin rdf:about=\"#Disjoin_"+title+"_"+workspace+"\"><rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/><Entity rdf:resource=\"#"+title+"\"/><Workspace rdf:resource=\"#"+workspace+"\"/>\n</Disjoin>";
		
			write("DTS",msg.getBytes());
		
			this.workspaces.remove(workspace);
		}
		return true;
	}

	//Function to print the debug
	private void debug(String info) {
		System.out.println(info);
	}
} 
