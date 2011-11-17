package br.ufu.facom.network.dlontology.msg;


public interface OWLParser {
	String parse(Message message);
	
	Message parseMessage(String owl);
}
