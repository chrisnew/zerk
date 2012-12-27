package de.chrisnew.zerk.math;

public class Line2D {
	private Vector2D startPoint, endPoint;

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

	private float createIntersectionDenominator(Line2D other) {
		float x1 = getStartPoint().getX();
		float x2 = getEndPoint().getX();
		float x3 = other.getStartPoint().getX();
		float x4 = other.getEndPoint().getX();
		float y1 = getStartPoint().getY();
		float y2 = getEndPoint().getY();
		float y3 = other.getStartPoint().getY();
		float y4 = other.getEndPoint().getY();

		return (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
	}

	public Vector2D getIntersection(Line2D other) {
		float x1 = getStartPoint().getX();
		float y1 = getStartPoint().getY();

		float x2 = getEndPoint().getX();
		float y2 = getEndPoint().getY();

		float x3 = other.getStartPoint().getX();
		float y3 = other.getStartPoint().getY();

		float x4 = other.getEndPoint().getX();
		float y4 = other.getEndPoint().getY();

		// TODO speed up

		float denominator = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

		// parallel.
		if (Math.abs(denominator) < 0.01) {
			return null;
		}

		float a = (x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4);
		float b = (x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4);

		return new Vector2D(a / denominator, b / denominator);
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
		return	point.getX() >= getStartPoint().getX() && point.getX() <= getEndPoint().getX() &&
				point.getY() >= getStartPoint().getY() && point.getY() <= getEndPoint().getY();
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
