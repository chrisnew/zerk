package de.chrisnew.zerk.game;

import de.chrisnew.zerk.math.Rectangle;
import de.chrisnew.zerk.math.Vector2D;
import de.chrisnew.zerk.net.CommandPacket;
import de.chrisnew.zerk.net.SimpleSerializable;

public class Area extends Rectangle implements SimpleSerializable {
	private GameMap gameMap = null;

	public Area(Vector2D pointA, Vector2D pointB, Vector2D pointC) {
		super(pointA, pointB, pointC);
	}

	public Area() {
		super(Vector2D.ORIGIN, Vector2D.ORIGIN, Vector2D.ORIGIN);
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

	public Vector2D getPointD() {
		return new Vector2D(getPointA().getX() + getPointB().getX(), getPointA().getY() + getPointC().getY());
	}

	public EntityCollection getEntities() {
		return getGameMap().getEntitiesInArea(this);
	}

	@Override
	public void serialize(CommandPacket dp) {
		dp.writeVector2D(getPointA());
		dp.writeVector2D(getPointB());
		dp.writeVector2D(getPointC());
		dp.writeString(getAreaName());
	}

	@Override
	public void unserialize(CommandPacket dp) {
		setPointA(dp.readVector2D());
		setPointB(dp.readVector2D());
		setPointC(dp.readVector2D());
		setAreaName(dp.readString());
	}
}
