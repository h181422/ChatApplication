package com.example.larsv.chatapplication.Messages;

//This is the parent of all other messages

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class MotherOfAllMessages implements ISendable{

	public static final int MESSAGE = 0;
	public static final int UPDATE_MESSAGE = 1;
	public static final int USERS_ONLINE_MESSAGE = 2;
	public static final int LOGIN_MESSAGE = 3;
	
	public static final int TYPE_SIZE = 2;
	public static final int LENGTH_SIZE = 2;
	public static final int HEADER_SIZE = TYPE_SIZE + LENGTH_SIZE;
	
	protected short type;
	protected short length;
	
	
	public MotherOfAllMessages() {
		setType();
	}
	public short getType() {
		return type;
	}
	
	protected abstract void setType();
	
	//Header is serialized equally for all messages
	protected byte[] serializeHeader() {
		ByteBuffer bb = ByteBuffer.allocate(TYPE_SIZE);
		bb.putShort(type);
		byte[] ar1 = bb.array();
		ByteBuffer bb2 = ByteBuffer.allocate(LENGTH_SIZE);
		bb2.putShort(length);
		byte[] ar2 = bb2.array();
		byte[] arBoth = new byte[HEADER_SIZE];
		arBoth[0] = ar1[0];
		arBoth[1] = ar1[1];
		arBoth[2] = ar2[0];
		arBoth[3] = ar2[1];
		return arBoth;
	}
	protected void extractHeader(byte[] fromBytes) {
		byte[] bType = new byte[TYPE_SIZE];
		bType[0] = fromBytes[0];
		bType[1] = fromBytes[1];
		ByteBuffer bb = ByteBuffer.wrap(bType).order(ByteOrder.BIG_ENDIAN);
		type = bb.getShort();
		byte[] bSize = new byte[LENGTH_SIZE];
		bSize[0] = fromBytes[2];
		bSize[1] = fromBytes[3];
		ByteBuffer bb2 = ByteBuffer.wrap(bSize).order(ByteOrder.BIG_ENDIAN);
		length = bb2.getShort();
	}
	
}
