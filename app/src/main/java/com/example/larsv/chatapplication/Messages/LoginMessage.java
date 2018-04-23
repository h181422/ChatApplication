package com.example.larsv.chatapplication.Messages;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class LoginMessage extends MotherOfAllMessages {

	public final static byte LOGIN = 0;
	public final static byte CREATE_ACCOUNT = 1;
	
	String username, password;
	byte usernameSize;
	byte passwordSize;
	byte typeL;
	
	public LoginMessage(String username, String password, byte typeL) {
		this.username = username;
		this.password = password;
		this.typeL = typeL;
		usernameSize = (byte) this.username.getBytes().length;
		passwordSize = (byte) this.password.getBytes().length;
	}
	
	public LoginMessage(byte[] fromBytes) {
		extractHeader(fromBytes);
		usernameSize = fromBytes[4];
		username = new String(Arrays.copyOfRange(fromBytes, 
				HEADER_SIZE+1, HEADER_SIZE+1+usernameSize));
		passwordSize = fromBytes[HEADER_SIZE+1+usernameSize];
		password = new String(Arrays.copyOfRange(fromBytes, 
				HEADER_SIZE+1+passwordSize, HEADER_SIZE
								+1+usernameSize+1+passwordSize));
		typeL = fromBytes[length-1];
	}
	
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
		this.usernameSize = (byte)this.username.getBytes().length;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
		this.passwordSize = (byte)this.password.getBytes().length;
	}
	

	public byte getTypeL() {
		return typeL;
	}

	public void setTypeL(byte typeL) {
		this.typeL = typeL;
	}

	@Override
	public byte[] serialize() {
		//magic numbers 1 is length of bytes: username, password and typeL
		length = (short)(serializeHeader().length+1+username.getBytes().length+1+password.getBytes().length+1);
		
		ByteBuffer serialized = ByteBuffer.allocate(length);
		
		serialized.put(serializeHeader());
		serialized.put((byte)username.getBytes().length);
		serialized.put(username.getBytes());
		serialized.put((byte)password.getBytes().length);
		serialized.put(password.getBytes());
		serialized.put((byte)typeL);
		// [MsgType Size usernameSize username passwordSize password]
		return serialized.array();
	}

	@Override
	protected void setType() {
		type = MotherOfAllMessages.LOGIN_MESSAGE;
	}

}
