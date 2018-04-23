package com.example.larsv.chatapplication.Messages;

/*
 * Simplifies the use of outputStreams for messages
 * by using code in messages: .serialize() from interface ISendable
 * which all messages implements
 */

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

public class MessageOutputStream {

	OutputStream out;
	
	public MessageOutputStream(OutputStream out) {
		this.out = out;
	}
	
	public void writeMessage(MotherOfAllMessages msg) throws SocketException {
		try {
			out.write(msg.serialize());
		} catch (SocketException ex) {
			throw ex;
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
}
