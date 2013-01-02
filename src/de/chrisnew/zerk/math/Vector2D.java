package de.chrisnew.zerk.math;

public class Vector2D {
	public static final Vector2D ORIGIN = new Vector2D(0, 0);

	private float x, y;

	public Vector2D() {
		this(0.f, 0.f);
	}

	public Vector2D(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Vector2D(Vector2D vec) {
		this.x = vec.x;
		this.y = vec.y;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public double length() {
		return Math.sqrt(getX() * getX() + getY() * getY());
	}

	public Vector2D substract(Vector2D other) {
		Vector2D vec = new Vector2D();

		vec.setX(getX() - other.getX());
		vec.setY(getY() - other.getY());

		return vec;
	}

	public Vector2D add(Vector2D other) {
		Vector2D vec = new Vector2D();

		vec.setX(getX() + other.getX());
		vec.setY(getY() + other.getY());

		return vec;
	}

	public boolean equals(Vector2D other) {
		return other.getX() == getX() && other.getY() == getY();
	}

	public boolean isNearby(Vector2D other) {
		return substract(other).length() <= 3;
	}

	@Override
	public String toString() {
		return "[" + getX() + ", " + getY() + "]";
	}

	public Vector2D scale(double factor) {
		return scale((float) factor);
	}

	public Vector2D scale(float factor) {
		return new Vector2D(getX() * factor, getY() * factor);
	}

	public String toTextRelatively() {
		if (getX() == 0 && getY() == 0) {
			return "here";
		}

		return toText();
	}

	public String toText() {
		if (getX() == 0 && getY() == 0) {
			return "origin";
		}

		StringBuilder sb = new StringBuilder();

		int aX = (int) Math.abs(getX());
		int aY = (int) Math.abs(getY());

		sb.append(aX);
		sb.append(" step");
		sb.append(aX == 1 ? "" : "s");

		if (getX() > 0) {
			sb.append(" east");
		} else {
			sb.append(" west");
		}

		sb.append(" and ");

		sb.append(aY);
		sb.append(" step");
		sb.append(aY == 1 ? "" : "s");

		if (getY() > 0) {
			sb.append(" north");
		} else {
			sb.append(" south");
		}

		return sb.toString();
	}

	public double dotProduct(Vector2D other) {
		return getX() * other.getY() - getY() * other.getX();
	}

	/**
	 *
	 * @param other null
	 * @return
	 */
	// CR: other's Z would be zero, so we ignore other at all (other = null)
	public Vector2D crossProduct(Vector2D other) {
		return new Vector2D(getY(), -getX());
	}
}
