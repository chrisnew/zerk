package de.chrisnew.zerk.math;

public abstract class ThreePointPolygon {
	private Vector2D pointA, pointB, pointC;

	public ThreePointPolygon(Vector2D pointA, Vector2D pointB, Vector2D pointC) {
		setPointA(pointA);
		setPointB(pointB);
		setPointC(pointC);
	}

	public Vector2D getPointA() {
		return pointA;
	}

	public void setPointA(Vector2D pointA) {
		this.pointA = pointA;
	}

	public Vector2D getPointB() {
		return pointB;
	}

	public void setPointB(Vector2D pointB) {
		this.pointB = pointB;
	}

	public Vector2D getPointC() {
		return pointC;
	}

	public void setPointC(Vector2D pointC) {
		this.pointC = pointC;
	}

	abstract public boolean isPointInArea(Vector2D point);
}
