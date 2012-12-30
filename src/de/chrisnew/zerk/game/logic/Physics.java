package de.chrisnew.zerk.game.logic;

import de.chrisnew.zerk.game.GameMap;
import de.chrisnew.zerk.game.Wall;
import de.chrisnew.zerk.game.entities.BaseEntity;
import de.chrisnew.zerk.game.entities.NPC;
import de.chrisnew.zerk.math.Line2D;
import de.chrisnew.zerk.math.Vector2D;

public class Physics {
	private final GameMap gameMap;

	public Physics(GameMap gamemap) {
		gameMap = gamemap;
	}

	public boolean tryEntityWalkTo(NPC npc, Vector2D position) {
//		return isImpassible(subject, object);

		return !isImpassibleByTraceLine(new Line2D(npc.getPosition(), position));
	}

	private boolean isVisibleByTraceLine(Line2D traceLine) {
		for (Wall wall : gameMap.getWalls()) {
			if (traceLine.intersects(wall)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * checks if view is not intersected by walls
	 *
	 * @param subject
	 * @param object
	 * @return
	 */
	public boolean isVisible(BaseEntity subject, BaseEntity object) {
		Line2D traceLine = new Line2D(subject.getPosition(), object.getPosition());

		return isVisibleByTraceLine(traceLine);
	}

	private boolean isImpassibleByTraceLine(Line2D traceLine) {
		return !isVisibleByTraceLine(traceLine); // TODO: check entities as well
	}

	/**
	 * check if traceline is intersected neither by entities nor by walls
	 *
	 * @param subject
	 * @param object
	 * @return
	 */
	public boolean isImpassible(BaseEntity subject, BaseEntity object) {
		Line2D traceLine = new Line2D(subject.getPosition(), object.getPosition());

		return isImpassibleByTraceLine(traceLine);
	}

	public boolean isAccessibleForUsage(BaseEntity subject, BaseEntity object) {
		// 1. pass: distance. should not be longer than 3 units.
		if (!subject.getPosition().isNearby(object.getPosition())) {
			return false;
		}

		return !isImpassible(subject, object);
	}


}
