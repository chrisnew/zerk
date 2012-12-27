package de.chrisnew.zerk.game.entities;

import de.chrisnew.zerk.game.VisibleEntity;


public class Dog extends NPC implements VisibleEntity {
	private int nextBark = 100000;

	private void resetBark() {
		nextBark = (int) Math.round(Math.random() * 10000);
	}

	@Override
	public void spawn() {
		super.spawn();
		resetBark();
	}

	@Override
	public void think() {
		super.think();

		if (nextBark-- < 0) {
			bark();
			resetBark();
		}
	}

	public void bark() {
		emitSound(.8f, "*bark*");
	}
}
