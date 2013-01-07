package de.chrisnew.zerk.game.entities;

import de.chrisnew.zerk.game.entities.annotation.AttributeGetter;
import de.chrisnew.zerk.game.entities.annotation.AttributeSetter;

public class Key extends InventoryItem {
	private String label = "Door";

	@AttributeGetter(value = "label")
	public String getLabel() {
		return label;
	}

	@AttributeSetter(value = "label")
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getInventoryName() {
		return getLabel() + " " + getClassname();
	}
}
