package de.chrisnew.zerk.game;

import de.chrisnew.zerk.math.AABB;
import de.chrisnew.zerk.math.Line2D;
import de.chrisnew.zerk.net.CommandPacket;
import de.chrisnew.zerk.net.SimpleSerializable;


public class Wall extends Line2D implements SimpleSerializable {
	public Wall() {
		this(0, 0, 0, 0);
	}

	public Wall(float i, float j, float k, float l) {
		super(i, j, k, l);
	}

	@Override
	public void serialize(CommandPacket dp) {
		dp.writeVector2D(getStartPoint());
		dp.writeVector2D(getEndPoint());
	}

	@Override
	public void unserialize(CommandPacket dp) {
		setStartPoint(dp.readVector2D());
		setEndPoint(dp.readVector2D());
	}

	public AABB getBoundingBox() {
		return null;
	}
}
