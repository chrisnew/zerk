package de.chrisnew.zerk.net;

public interface SimpleSerializable {
	public void serialize(CommandPacket dp);
	public void unserialize(CommandPacket dp);
}
