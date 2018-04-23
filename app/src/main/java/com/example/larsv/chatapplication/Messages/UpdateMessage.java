package com.example.larsv.chatapplication.Messages;

//This message should be sent as a message to request information

import java.nio.ByteBuffer;
import java.util.Arrays;

public class UpdateMessage extends MotherOfAllMessages{

	public static final byte EXIT = -1;
	@Deprecated
	public static final byte MY_NAME_IS = 0;
	public static final byte REQUEST_USERS_ONLINE = 1;
	public static final byte NOT_ONLINE = 2;
	public static final byte IS_ONLINE = 3;
	public static final byte ASK_IF_ONLINE = 4;
	public static final byte LOGIN_OK = 5;
	public static final byte LOGIN_FAILED = 6;
	public static final byte CREATE_ACCOUNT_OK = 7;
	public static final byte CREATE_ACCOUNT_FAILED = 8;
	
	String username;
	byte status;
	
	public UpdateMessage(String username) {
		this.username = username;
		length = (short) (username.length() + 2 + 2 + 1);
		status = 0;
	}
	public UpdateMessage(String username, byte status) {
		this.username = username;
		length = (short) (username.length() + 2 + 2 + 1);
		this.status = status;
	}
	
	public UpdateMessage(byte[] fromBytes) {
		extractHeader(fromBytes);
		username = new String(Arrays.copyOfRange(fromBytes, 4, length-1));
		status = fromBytes[length-1];
	}
	
	@Override
	public byte[] serialize() {
		length = (short) (username.length() + 2 + 2 + 1);
		ByteBuffer serialized = ByteBuffer.allocate(length);
		
		serialized.put(serializeHeader());
		serialized.put(username.getBytes());
		serialized.put((byte)status);
		//[type size username status]
		return serialized.array();
	}

	public String getUsername() {
		return username;
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}
	public String toString() {
		return type +" "+ length +" "+ username +" "+ status;
	}

	@Override
	protected void setType() {
		type = MotherOfAllMessages.UPDATE_MESSAGE;
	}

}
