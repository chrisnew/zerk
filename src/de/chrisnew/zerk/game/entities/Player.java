package de.chrisnew.zerk.game.entities;

import java.util.LinkedList;
import java.util.List;

import de.chrisnew.zerk.game.VisibleEntity;
import de.chrisnew.zerk.game.entities.annotation.EntityInfo;
import de.chrisnew.zerk.net.CommandPacket;

@EntityInfo(description="Entity which describes the actual player.", virtual = true)
public class Player extends NPC implements VisibleEntity {
	private String name = "Unnamed";

	private final List<InventoryItem> inventory = new LinkedList<>();

	@Override
	public void serialize(CommandPacket dp) {
		super.serialize(dp);

		synchronized (inventory) {
			dp.writeInteger(inventory.size());

			for (InventoryItem item : inventory) {
				dp.writeEntity(item);
			}
		}

		dp.writeString(name);
	}

	@Override
	public void unserialize(CommandPacket dp) {
		super.unserialize(dp);

		synchronized (inventory) {
			inventory.clear();

			for (int i = dp.readInteger(); i != 0; i--) {
				inventory.add((InventoryItem) dp.readEntity());
			}
		}

		name = dp.readString();
	}

	public List<InventoryItem> getInventory() {
		return inventory;
	}

	public void addItemToInventory(InventoryItem item) {
		inventory.add(item);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
