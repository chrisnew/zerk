package de.chrisnew.zerk.math;

public class Rectangle {
	private Vector2D pointA, pointB, pointC;

	public Rectangle(Vector2D pointA, Vector2D pointB, Vector2D pointC) {
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

	public double getArea() {
		return getPointA().substract(getPointB()).length() * getPointA().substract(getPointC()).length();
	}

	public boolean isPointInArea(Vector2D point) {
		if (point.getY() == getPointB().getY()) {
		return (
			point.getX() >= getPointA().getX() && point.getX() <= getPointB().getX() &&
			point.getY() >= getPointA().getY() && point.getY() <= getPointC().getY()
//		) || (
//			point.getX() >= getPointA().getX() && point.getX() <= getPointC().getX() &&
//			point.getY() >= getPointA().getY() && point.getY() <= getPointB().getY()
		); } else {
			return (point.getX() >= getPointA().getX() && point.getX() <= getPointC().getX() &&
			point.getY() >= getPointA().getY() && point.getY() <= getPointB().getY());
		}
	}
}
