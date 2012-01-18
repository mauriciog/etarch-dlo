package br.ufu.facom.network.dlontology.msg;


public interface OWLParser {
	byte[] parse(Message message);
	
	Message parseMessage(byte[] data);

	boolean validStartMessage(byte[] bytes);
	boolean validEndMessage(byte[] bytes, int offset);
}
