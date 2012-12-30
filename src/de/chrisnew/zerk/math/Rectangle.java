package de.chrisnew.zerk.math;

public class Rectangle extends ThreePointPolygon {
	public Rectangle(Vector2D pointA, Vector2D pointB, Vector2D pointC) {
		super(pointA, pointB, pointC);
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

	public double getArea() {
		return getPointA().substract(getPointB()).length() * getPointA().substract(getPointC()).length();
	}
}
