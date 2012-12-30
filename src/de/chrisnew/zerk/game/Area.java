package de.chrisnew.zerk.game;

import de.chrisnew.zerk.math.Rectangle;
import de.chrisnew.zerk.math.Vector2D;

public class Area extends Rectangle {
	private GameMap gameMap = null;

	public Area(Vector2D pointA, Vector2D pointB, Vector2D pointC) {
		super(pointA, pointB, pointC);
	}

	private String areaName = "Area";

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public GameMap getGameMap() {
		return gameMap;
	}

	public void setGameMap(GameMap gameMap) {
		this.gameMap = gameMap;
	}

	public EntityCollection getEntities() {
		return getGameMap().getEntitiesInArea(this);
	}
}
