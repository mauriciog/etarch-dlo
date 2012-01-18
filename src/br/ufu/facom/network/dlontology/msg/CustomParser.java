package br.ufu.facom.network.dlontology.msg;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;


public class CustomParser implements OWLParser{
	//PatternsMessage
	private static final Pattern messagePattern = Pattern.compile(
							"<Message rdf:about=\"#(.*?)\">.*?"+
							"<Source rdf:resource=\"#(.*?)\"/>"+
							"<Destination rdf:resource=\"#(.*?)\"/>.*?"+
							"<Payload rdf:string=\"(.*?)\"/>.*?" +
							"</Message>",Pattern.DOTALL);
							
	private static final byte[] openTag = "<Message".getBytes();
	private static final byte[] closeTag = "</Message>".getBytes();
	
	public byte[] parse(Message message){
		String msg = "<Message rdf:about=\"#"+message.getLabel()+"\">"+
			"<rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>"+
			"<Source rdf:resource=\"#"+message.getSource()+"\"/>"+
			"<Destination rdf:resource=\"#"+message.getDestination()+"\"/>"+
			"<Payload rdf:string=\""+new String(Base64.encode(message.getPayload()))+"\"/>"+
		"</Message>";
		
		return msg.getBytes();
	}
	
	public Message parseMessage(byte[] data){
		
		Matcher matcher;
		
		matcher = messagePattern.matcher(new String(data));
		if(matcher.find()){
			try {
				return new Message(matcher.group(1),matcher.group(2),matcher.group(3),Base64.decode(matcher.group(4).getBytes()));
			} catch (Exception e) {
				//System.err.println("Message non-valid...");
				//e.printStackTrace();
			}
		}
		
		return null;
	}

	@Override
	public boolean validStartMessage(byte[] bytes) {
		int sizeOpenTag = openTag.length;
		int sizeBytes = bytes.length;
		
		if(sizeBytes >= sizeOpenTag){
			for(int i=0; i<sizeOpenTag; i++){
				if(openTag[i] != bytes[i])
					return false;
			}
			return true;
		}else
			return false;
	}

	@Override
	public boolean validEndMessage(byte[] bytes,int offset) {
		int sizeOpenTag = openTag.length;
		int sizeBytes = offset;
		int sizeCloseTag = closeTag.length;
		
		if(sizeBytes >= (sizeOpenTag + sizeCloseTag)){
			for(int i=0; i<sizeCloseTag; i++){
				int indexBytes = sizeBytes - 1 - i;
				int indexClose = sizeCloseTag - 1 - i;

				if(closeTag[indexClose] != bytes[indexBytes])
					return false;
			}
			return true;
		}else
			return false;
	}
}
