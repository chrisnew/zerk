package de.chrisnew.zerk.net;

/**
 * implement this to allow transmitting object data via a CommandPacket
 *
 * @author CR
 *
 */
public interface SimpleSerializable {
	public void serialize(CommandPacket dp);
	public void unserialize(CommandPacket dp);
}
