package de.chrisnew.zerk.game.entities;

import de.chrisnew.zerk.game.VisibleEntity;
import de.chrisnew.zerk.math.Vector2D;

abstract public class NPC extends BaseEntity implements VisibleEntity {
	/**
	 * actually, this should happen in think..
	 * @param direction
	 */
//	final public void walkBy(Vector2D direction) {
//		// update locally
//		walkByOffline(direction);
//
//		// send walk command to server
//		Client.sendDataPacket(new DataPacket(PacketClass.ENTITY_C2S_NPC_WALKBY, 0).writeInteger(getId()).writeVector2D(direction));
//	}

	public void walkBy(Vector2D direction) {
		setPosition(getPosition().add(direction));
	}
}
