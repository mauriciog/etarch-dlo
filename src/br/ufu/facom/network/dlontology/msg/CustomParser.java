package br.ufu.facom.network.dlontology.msg;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CustomParser implements OWLParser{
	//PatternsMessage
	private static Pattern messagePattern = Pattern.compile(
							"<Message rdf:about=\"#(.*?)\">.*?"+
							"<Source rdf:resource=\"#(.*?)\"/>"+
							"<Destination rdf:resource=\"#(.*?)\"/>.*?"+
							"<Payload rdf:string=\"(.*?)\"/>.*?" +
							"</Message>",Pattern.DOTALL);
							
	
	public String parse(Message message){
		return 
		"<Message rdf:about=\"#"+message.getLabel()+"\">"+
			"<rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>"+
			"<Source rdf:resource=\"#"+message.getSource()+"\"/>"+
			"<Destination rdf:resource=\"#"+message.getDestination()+"\"/>"+
			"<Payload rdf:string=\""+message.getPayload()+"\"/>"+
		"</Message>";
	}
	
	public Message parseMessage(String owl){
		
		Matcher matcher;
		
		matcher = messagePattern.matcher(owl);
		if(matcher.find()){
			return new Message(matcher.group(1),matcher.group(2),matcher.group(3),matcher.group(4));
		}
		
		return null;
	}
}
