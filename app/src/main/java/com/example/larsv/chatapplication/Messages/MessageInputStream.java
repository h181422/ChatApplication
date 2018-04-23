package com.example.larsv.chatapplication.Messages;

/*
 * Simplifies the use of input streams for messages.
 * returns the input it receives as a message
 * returns null to indicate an ended connection
 */


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MessageInputStream {

	
	
	InputStream in;
	MotherOfAllMessages msg;
	
	public MessageInputStream(InputStream in) {
		this.in = in;
		
	}
	
	public MotherOfAllMessages readMessage() throws IOException {
		if(in == null) {
			System.out.println("InputStream is null, returning null");
			return null;
		}
		try {
		//Read the 4 first bytes of the incoming message
		//to determine the length of the whole message.
		int totalBytesReceived = MotherOfAllMessages.HEADER_SIZE;
		int bytesReceived;
		short length;
		short type;
		byte[] typeHolder = new byte[MotherOfAllMessages.TYPE_SIZE];
		byte[] lengthHolder = new byte[MotherOfAllMessages.LENGTH_SIZE];
		
		typeHolder[0] = (byte)in.read();
		typeHolder[1] = (byte)in.read();
		ByteBuffer bb = ByteBuffer.wrap(typeHolder)
				.order(ByteOrder.BIG_ENDIAN);
		type = bb.getShort();
		
		lengthHolder[0] = (byte)in.read();
		lengthHolder[1] = (byte)in.read();
		ByteBuffer bb2 = ByteBuffer.wrap(lengthHolder)
				.order(ByteOrder.BIG_ENDIAN);
		length = bb2.getShort();
		
		//System.out.println("Receiving message of type " + type + " and length " + length +".");
		
		byte[] buffer = new byte[length];
		buffer[0] = typeHolder[0];
		buffer[1] = typeHolder[1];
		buffer[2] = lengthHolder[0];
		buffer[3] = lengthHolder[1];
		
		//Then read the rest of the message to a buffer
		while(totalBytesReceived < length) {
			if((bytesReceived = in.read(buffer, totalBytesReceived,
					length-totalBytesReceived)) == -1) {
				System.out.println("-1 returned from in.read()");
				return null;
			}
			totalBytesReceived += bytesReceived;
		}
		
		if (type == MotherOfAllMessages.MESSAGE) {
			msg = new Message(buffer);			
		} else if(type == MotherOfAllMessages.UPDATE_MESSAGE) {
			msg = new UpdateMessage(buffer);
		} else if(type == MotherOfAllMessages.USERS_ONLINE_MESSAGE) {
			msg = new UsersOnline(buffer);
		} else if(type == MotherOfAllMessages.LOGIN_MESSAGE) {
			msg = new LoginMessage(buffer);
		}
		
		return msg;
		
		
		} catch (NegativeArraySizeException e) {
			//e.printStackTrace();
			return null;
		}
	}
	
	
	
}
