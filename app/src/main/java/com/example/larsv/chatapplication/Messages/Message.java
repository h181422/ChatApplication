package com.example.larsv.chatapplication.Messages;

/*
 * Code to serialize and deserialize a regular message
 */

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Message extends MotherOfAllMessages{

	final int MAXNAMELENGTH = Byte.MAX_VALUE;
	final static int MAX_CONTENT_LENGTH = 65000; 
	final static int CONTENT_SIZE = 2;
	final static int FROM_TO_SIZE = 2;
	
	String from;
	String to;
	String content;
	byte fromSize;
	byte toSize;

	public Message(String message, String receiver, String me) {
		type = MotherOfAllMessages.MESSAGE;
		setTo(receiver);
		setFrom(me);
		setContent(message);
		length = (short)(TYPE_SIZE+LENGTH_SIZE+CONTENT_SIZE+to.getBytes().length+
				from.getBytes().length+content.getBytes().length);
		toSize = (byte)to.length();
		fromSize = (byte)from.length();
	}
	public Message(byte[] fromBytes) {
		extractHeader(fromBytes);
		toSize = fromBytes[4];
		to = new String(Arrays.copyOfRange(fromBytes, 
				HEADER_SIZE+1, HEADER_SIZE+1+toSize));
		fromSize = fromBytes[HEADER_SIZE+1+toSize];
		from = new String(Arrays.copyOfRange(fromBytes, 
				HEADER_SIZE+1+toSize+1, HEADER_SIZE
								+1+toSize+1+fromSize));
		content = new String(Arrays.copyOfRange(fromBytes, 
				HEADER_SIZE+1+toSize+1+fromSize, length));
	}
	
	public short getType() {
		return type;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		if(from.length() >= MAXNAMELENGTH)
			throw new IllegalArgumentException("Too long username."+
										"Max length = " + MAXNAMELENGTH);
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		if(to.length() >= MAXNAMELENGTH)
			throw new IllegalArgumentException("Too long recivername."+
										"Max length = " + MAXNAMELENGTH);
		this.to = to;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		if(content.length() > MAX_CONTENT_LENGTH)
			throw new IllegalArgumentException("Too long message."+
				"Split up the message or use a different message type");
		this.content = content;
	}


	
	@Override
	public byte[] serialize() {
		length = (short)(FROM_TO_SIZE+FROM_TO_SIZE+CONTENT_SIZE+
				to.getBytes().length+from.getBytes().length+content.getBytes().length);
		
		ByteBuffer serialized = ByteBuffer.allocate(length);
		
		serialized.put(serializeHeader());
		serialized.put((byte)to.length());
		serialized.put(to.getBytes());
		serialized.put((byte)from.length());
		serialized.put(from.getBytes());
		serialized.put(content.getBytes());
		// [MsgType Size toSize To fromSize From Content]
		return serialized.array();
	}
	
	public Message toMessage(byte[] fromBytes) {
		extractHeader(fromBytes);
		toSize = fromBytes[4];
		to = new String(Arrays.copyOfRange(fromBytes, 5, 5+toSize));
		fromSize = fromBytes[5+toSize];
		from = new String(Arrays.copyOfRange(fromBytes, 5+toSize+1, 5+toSize+1+fromSize));
		content = new String(Arrays.copyOfRange(fromBytes, 5+toSize+1+fromSize, length));
		// [MsgType Size toSize To fromSize From Content]
		return this;		
	}
	
	public String toString() {
		return from + ": " + content;
	}
	@Override
	protected void setType() {
		type = MotherOfAllMessages.MESSAGE;
	}
	
}
