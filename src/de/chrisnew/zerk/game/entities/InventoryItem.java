package de.chrisnew.zerk.game.entities;

import de.chrisnew.zerk.Console;

abstract public class InventoryItem extends Item {
	@Override
	public void use(BaseEntity user) {
		if (user instanceof Player) {
			((Player) user).addItemToInventory(this);
		}
	}

	/**
	 * local only
	 */
	public void activate() {
		Console.info("This item can't be activated.");
	}

	/**
	 * local only
	 */
	public String getInventoryName() {
		return getClassname();
	}

	@Override
	public String toString() {
		return getInventoryName();
	}
}
