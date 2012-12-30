package de.chrisnew.zerk.math;

public class Line2D {
	private Vector2D startPoint, endPoint;

	public Line2D() {
		this(new Vector2D(), new Vector2D());
	}

	public Line2D(Vector2D s, Vector2D e) {
		setStartPoint(s);
		setEndPoint(e);
	}

	public Line2D(float x1, float y1, float x2, float y2) {
		this(new Vector2D(x1, y1), new Vector2D(x2, y2));
	}

	public Vector2D getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(Vector2D startPoint) {
		this.startPoint = startPoint;
	}

	public Vector2D getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(Vector2D endPoint) {
		this.endPoint = endPoint;
	}

	private double createIntersectionDenominator(Line2D other) {
		Vector2D v1 = getEndPoint().substract(getStartPoint());
		Vector2D v2 = other.getEndPoint().substract(other.getStartPoint());

		return v1.dotProduct(v2);
	}

	public Vector2D getIntersection(Line2D other) {
		Vector2D v1 = getEndPoint().substract(getStartPoint());
		Vector2D v2 = other.getEndPoint().substract(other.getStartPoint());

		double dp12 = v1.dotProduct(v2);

		if (dp12 == 0) { // same vectors, so the lines are parallel
			return null;
		}

		double v3dp1 = getStartPoint().dotProduct(getEndPoint());
		double v3dp2 = other.getStartPoint().dotProduct(other.getEndPoint());

		Vector2D v3 = new Vector2D(
			(float) (v3dp1 * (other.getStartPoint().getX() - other.getEndPoint().getX())) - (float) (v3dp2 * (getStartPoint().getX() - getEndPoint().getX())),
			(float) (v3dp1 * (other.getStartPoint().getY() - other.getEndPoint().getY())) - (float) (v3dp2 * (getStartPoint().getY() - getEndPoint().getY()))
		);

		return v3.scale(1 / dp12);
	}

	/**
	 * checks intersection for line segment
	 */
	public boolean intersects(Line2D other) {
		Vector2D p = getIntersection(other);

		if (p == null) {
			return false;
		}

		return (isPointOnLine(p) && other.isPointOnLine(p));
	}

	public boolean parallelTo(Line2D other) {
		return Math.abs(createIntersectionDenominator(other)) < 0.01;
	}

	public boolean isPointOnLine(Vector2D point) {
		return	(point.getX() >= getStartPoint().getX() && point.getX() <= getEndPoint().getX() &&
				 point.getY() >= getStartPoint().getY() && point.getY() <= getEndPoint().getY()) ||
				(point.getX() >= getEndPoint().getX() && point.getX() <= getStartPoint().getX() &&
				 point.getY() >= getEndPoint().getY() && point.getY() <= getStartPoint().getY());
	}

	public boolean equals(Line2D obj) {
		return super.equals(obj) || (
			getEndPoint().equals(obj.getEndPoint()) &&
			getStartPoint().equals(obj.getStartPoint())
		);
	}

	public double length() {
		return getEndPoint().substract(getStartPoint()).length();
	}

	@Override
	public String toString() {
		return getStartPoint() + " -> " + getEndPoint();
	}
}
