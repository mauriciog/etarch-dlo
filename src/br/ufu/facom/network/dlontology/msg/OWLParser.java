package br.ufu.facom.network.dlontology.msg;

import java.util.List;


public interface OWLParser {
	byte[] parse(Message message);
	
	Message parseMessage(byte[] data);

	boolean validStartMessage(byte[] bytes);
	boolean validEndMessage(byte[] bytes, int offset);
	List<Message> fragmentMessage(String title, int vlan, String destin, byte[] msg);
}
