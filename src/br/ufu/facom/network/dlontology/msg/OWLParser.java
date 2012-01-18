package br.ufu.facom.network.dlontology.msg;

import java.util.List;


public interface OWLParser {
	byte[] parse(Message message);
	
	Message parseMessage(byte[] data);

	boolean validStartMessage(byte[] bytes);
	boolean validEndMessage(byte[] bytes, int offset);
<<<<<<< HEAD
	List<Message> fragmentMessage(String title, int vlan, String destin, byte[] msg);
=======
>>>>>>> fb0076293af8b30c1ebec54aa11e973b8d797d60
}
