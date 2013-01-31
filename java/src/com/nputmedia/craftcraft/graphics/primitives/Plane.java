package com.nputmedia.craftcraft.graphics.primitives;

public class Plane {
	public Point3f normal = new Point3f(0, 0, 0);
	public Point3f point = new Point3f(0, 0, 0);

	public float distanceTo(Point3f aPoint) {
		return aPoint.subtract(point).dot(normal);
	}

	@Override
	public String toString() {
		return String.format("Plane[p=%s, n=%s]", point, normal);
	}
}
