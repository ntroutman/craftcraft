package com.nputmedia.craftcraft.graphics.primitives;

public class Cone {
	public Point3f vertex = null;
	public Point3f axis = null;
	public float angle = 0;

	public Cone() {

	}

	public boolean intersectsSphere(Point3f center, float radius) {
		return true;
	}
}
