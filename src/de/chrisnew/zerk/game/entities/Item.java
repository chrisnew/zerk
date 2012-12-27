package de.chrisnew.zerk.game.entities;

import de.chrisnew.zerk.game.VisibleEntity;



abstract public class Item extends BaseEntity implements VisibleEntity {
	@Override
	public boolean canUse() {
		return true;
	}
}
