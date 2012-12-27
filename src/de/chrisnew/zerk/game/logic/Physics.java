package de.chrisnew.zerk.game.logic;

import de.chrisnew.zerk.game.GameMap;
import de.chrisnew.zerk.game.entities.NPC;
import de.chrisnew.zerk.math.Vector2D;

public class Physics {
	private final GameMap gameMap;

	public Physics(GameMap gamemap) {
		gameMap = gamemap;
	}

	public boolean tryEntityWalkTo(NPC npc, Vector2D position) {
		return gameMap.isVisibleFor(npc.getPosition(), position);
	}

}
