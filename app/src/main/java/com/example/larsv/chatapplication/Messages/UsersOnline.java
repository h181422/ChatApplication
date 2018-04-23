package com.example.larsv.chatapplication.Messages;

//This message contains a list of all online users

import com.example.larsv.chatapplication.ConnectionHandlers.HostConnection;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;


public class UsersOnline extends MotherOfAllMessages{
	
	List<HostConnection> hostConnectionList;
	List<String> usersStringList;
	short amount;
	
	public UsersOnline(List<HostConnection> hostConnectionList) {
		this.hostConnectionList = hostConnectionList;
	}
	
	public UsersOnline(byte[] fromBytes) {
		usersStringList = new ArrayList<String>();
		extractHeader(fromBytes);
		byte[] bAmount = new byte[2];
		bAmount[0] = fromBytes[4];
		bAmount[1] = fromBytes[5];
		ByteBuffer bb = ByteBuffer.wrap(bAmount).order(ByteOrder.BIG_ENDIAN);
		amount = bb.getShort();
		int current = 6;
		for(int i = 0; i<amount;i++) {
			byte b = fromBytes[current];
			byte[] ba = new byte[b];
			current++;
			for(int j = 0; j < b; j++) {
				//System.out.println(current + "  "+j+"  " + fromBytes.length + "     " + ba.length);
				ba[j] = fromBytes[current];
				current++;
			}
			usersStringList.add(new String(ba));
			
		}
		//[type length numberOfUsers [lengthOfUser-1 User-1] ... [lengthOfUser-n User-n]] n=numberOfUSers	
	}
	public List<String> toStringList(){
		return usersStringList;
	}
	public String[] toStringArray(){
		return usersStringList.toArray(new String[0]);
	}
	
	@Override
	public byte[] serialize() {
		length = 6;
		byte[] bamount;
		synchronized(hostConnectionList) {
			for(HostConnection hc : hostConnectionList) {
				if (hc == null)
					hostConnectionList.remove(hc);
				else if(hc.getUsername() != null)
					length += 1 + hc.getUsername().getBytes().length;
			}			
			bamount = ByteBuffer.allocate(2).putShort((short)(hostConnectionList.size())).array();
		}
		int buffIndex = 6;
		byte[] buffer = new byte[length];
		byte[] header = serializeHeader();
				
		buffer[0] = header[0];
		buffer[1] = header[1];
		buffer[2] = header[2];
		buffer[3] = header[3];
		buffer[4] = bamount[0];		
		buffer[5] = bamount[1];
		synchronized(hostConnectionList) {
			for(int i = 0; i < hostConnectionList.size(); i++) {
				if(hostConnectionList.get(i) != null) {
					if(hostConnectionList.get(i).getUsername() != null) {
						String name = hostConnectionList.get(i).getUsername();
						int size = name.length();
						byte[] bname = name.getBytes();
						byte[] usr = new byte[size+1];
						
						usr[0] = (byte)size;
						for(int j = 0; j<bname.length;j++)
							usr[(j+1)] = bname[j];
						for(int j = 0; j < usr.length;j++) {
							if(j+buffIndex > length)
								return null;
							buffer[j+buffIndex] = usr[j];
						}
						buffIndex += usr.length;					
					}
				}
			}
		}
		//[type length numberOfUsers [lengthOfUser-1 User-1] ... [lengthOfUser-n User-n]] n=numberOfUSers
		return buffer;
	}


	@Override
	protected void setType() {
		type = 2;
		
	}
	public String toString() {
		String name = "";
		if(hostConnectionList != null) {
			synchronized(hostConnectionList) {
				if (hostConnectionList.isEmpty())
					return "Empty";
				for(HostConnection hc : hostConnectionList) 
					name += hc.getUsername() + ", ";
			}
		}else {
				if(usersStringList.isEmpty())
					return "Empty";
				for(String s : usersStringList)
					name += s + ", ";
		}
		return name;			
	}
}
