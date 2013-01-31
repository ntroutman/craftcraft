package com.nputmedia.craftcraft.graphics.primitives;


public class Sphere {
	public Point3f center = null;
	public float radius = 0;

	public Sphere() {
		center = new Point3f();
	}

	public boolean intersects( Sphere sphere ) {
		return intersectsSphere(sphere.center, sphere.radius);
	}

	public boolean intersectsSphere( Point3f center, float radius ) {
		return this.center.subtract(center).magnitudeSquare() < ( this.radius + radius );
	}
}
