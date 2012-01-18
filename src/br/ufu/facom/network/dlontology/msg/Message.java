package br.ufu.facom.network.dlontology.msg;

public class Message {
	private String label;
	private String source;
	private String destination;
	private byte[] payload;
	
	public Message(String source, String destination,byte[] payload){
		this.label = "M"+((int)(Math.random()*10000));
		this.source = source;
		this.destination = destination;
		this.payload = payload;
	}
	
	
	

	public Message(String label, String source, String destination,byte[] payload) {
		this(source,destination,payload);
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
	}
}
