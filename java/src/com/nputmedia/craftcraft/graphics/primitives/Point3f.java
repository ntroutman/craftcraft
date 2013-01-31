package com.nputmedia.craftcraft.graphics.primitives;

/**
 * Point3d
 * 
 * Simple 3D point class. It acts like points and vectors all at the same time. It is left up to the
 * programmer to keep straight which he intends any particular instance to be. The benefit of this
 * is that methods only need to accept on type of thing. After all a vector positioned to the orgin
 * is nothing more than a point.
 * 
 * @author Nathaniel Troutman
 * @version $Revision$ ($Author$)
 */
public class Point3f {
	public static final Point3f ZERO = new Point3f(0, 0, 0);

	// The three unit vectors
	public static final Point3f UNIT_X = new Point3f(1, 0, 0);

	public static final Point3f UNIT_Y = new Point3f(0, 1, 0);

	public static final Point3f UNIT_Z = new Point3f(0, 0, 1);

	// The x,y,z coordinate the point-vector
	public float x = 0f;

	public float y = 0f;

	public float z = 0f;

	/**
	 * 
	 * Creates a point from the three coordinates
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public Point3f( float x, float y, float z ) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * 
	 * Creates a point from the coordinates in the array
	 * 
	 * @param coord
	 */
	public Point3f( float[] coord ) {
		this.x = coord[0];
		this.y = coord[1];
		this.z = coord[2];
	}

	/**
	 * 
	 * Creates a new point that is a copy of the given point
	 * 
	 * @param point
	 */
	public Point3f( Point3f point ) {
		this.x = point.x;
		this.y = point.y;
		this.z = point.z;
	}

	public Point3f() {
		x = y = z = 0;
	}

	/**
	 * 
	 * Sets this point to the coordinates in the given point
	 * 
	 * @param source
	 *            the point to copy
	 * @return this point
	 */
	public Point3f set( Point3f source ) {
		x = source.x;
		y = source.y;
		z = source.z;

		return this;
	}

	/**
	 * 
	 * Sets this point to the three coordinates
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * 
	 * @return this point
	 */
	public Point3f set( float x, float y, float z ) {
		this.x = x;
		this.y = y;
		this.z = z;

		return this;
	}

	/**
	 * 
	 * The distance between this point and the origin
	 * 
	 * @return the distance
	 */
	public float magnitude() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}

	/**
	 * 
	 * The dot product of this point-vector with the given point-vector.
	 * 
	 * @param v
	 *            the point-vector to dot with
	 * @return the dot product
	 */
	public float dot( Point3f v ) {
		return x * v.x + y * v.y + z * v.z;
	}

	/**
	 * 
	 * Normalizes the vector and returns the normalized version. Does not change this vector.
	 * 
	 * @return the normalized verstion of this vector.
	 */
	public Point3f normalize() {
		float dist = magnitude();

		dist = ( dist > 0 ) ? ( dist ) : ( 1 );

		return new Point3f(x / dist, y / dist, z / dist);
	}

	/**
	 * 
	 * Normalizes this vector, and sets this vector to is normalized self.
	 * 
	 * @return this vector, now normalized
	 */
	public Point3f normalizeLocal() {
		float dist = magnitude();

		dist = ( dist > 0 ) ? ( dist ) : ( 1 );

		x /= dist;
		y /= dist;
		z /= dist;

		return this;
	}

	/**
	 * 
	 * Subtract the two point-vectors and return the difference. Does not change this vector.
	 * 
	 * @param v
	 *            the point-vector to subtract of this one
	 * 
	 * @return the difference of the two vectors
	 */
	public Point3f subtract( Point3f v ) {
		return new Point3f(x - v.x, y - v.y, z - v.z);
	}

	/**
	 * 
	 * Subtracts the given vector from this vector and sets this vector to the difference.
	 * 
	 * @param v
	 *            the vector to subtract of this one.
	 * 
	 * @return this vector which is now the difference
	 */
	public Point3f subtractLocal( Point3f v ) {
		x -= v.x;
		y -= v.y;
		z -= v.z;

		return this;
	}

	/**
	 * 
	 * Adds the given point-vector to this one and return the sum. Does not change this vector.
	 * 
	 * @param v
	 *            the point-vector to add on.
	 * 
	 * @return the sum of this point-vector with the other one.
	 */
	public Point3f add( Point3f v ) {
		return new Point3f(x + v.x, y + v.y, z + v.z);
	}

	/**
	 * 
	 * Adds the given vector onto this one, and sets this vector to the sum.
	 * 
	 * @param v
	 *            the point-vector to add on to this one.
	 * 
	 * @return this vector which is now the sum
	 */
	public Point3f addLocal( Point3f v ) {
		x += v.x;
		y += v.y;
		z += v.z;

		return this;
	}

	/**
	 * 
	 * Returns the cross product of this vector and the given one. Does not change this vector.
	 * 
	 * @param v
	 *            the vector to cross this one with.
	 * 
	 * @return the cross product
	 */
	public Point3f cross( Point3f v ) {
		return new Point3f(y * v.z - z * v.y, -x * v.z + z * v.x, x * v.y - y
		        * v.x);
	}

	/**
	 * 
	 * Crosses this vector with the given vector, and sets this vector to that cross product.
	 * 
	 * @param v
	 *            the vector to cross this one with
	 * 
	 * @return this vector, which is now the cross product
	 */
	public Point3f crossLocal( Point3f v ) {
		float tx = x, ty = y, tz = z;
		x = ty * v.z - tz * v.y;
		y = -tx * v.z + tz * v.x;
		z = tx * v.y - ty * v.x;

		return this;
	}

	/**
	 * 
	 * Scales this vector by the given scalar and returns the scaled vector. Does not change this
	 * vector.
	 * 
	 * @param scalar
	 *            the scalar to scale the vector by
	 * 
	 * @return the scaled vector
	 */
	public Point3f scale( float scalar ) {
		return new Point3f(x * scalar, y * scalar, z * scalar);
	}

	/**
	 * 
	 * Scales this vector by the given scalar, and sets this vector to that scaled vector.
	 * 
	 * @param scalar
	 *            the scalar to scale the vector by
	 * 
	 * @return this vector, which has now been scaled
	 */
	public Point3f scaleLocal( float scalar ) {
		x *= scalar;
		y *= scalar;
		z *= scalar;

		return this;
	}

	/**
	 * 
	 * Returns a new vector that is the transformation of this one.
	 * 
	 * @param mat
	 *            the matrix to do the transformations by
	 * @return the new vector
	 */
	public Point3f transformByMatrix( float[] mat ) {
		return new Point3f(mat[0] * x + mat[4] * y + mat[8] * z, mat[1] * x
		        + mat[5] * y + mat[9] * z, mat[2] * x + mat[6] * y + mat[10]
		        * z);
	}

	/**
	 * 
	 * Does a local transformation of the vector.
	 * 
	 * @param mat
	 *            the matrix to do the transformations by
	 * @return this vector.
	 */
	public Point3f transformByMatrixLocal( float[] mat ) {
		float xt = mat[0] * x + mat[4] * y + mat[8] * z;
		float yt = mat[1] * x + mat[5] * y + mat[9] * z;
		float zt = mat[2] * x + mat[6] * y + mat[10] * z;
		x = xt;
		y = yt;
		z = zt;

		return this;
	}

	@Override
	public String toString() {
		return String.format("Point3f[%f, %f, %f]", x, y, z);
	}

	public float magnitudeSquare() {
		return ( x * x ) + ( y * y ) + ( z * z );
	}

	public Point3f add( float x, float y, float z ) {
		Point3f point = new Point3f(this);
		point.x += x;
		point.y += y;
		point.z += z;
		return point;
	}

	public Point3f addLocal( float x, float y, float z ) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public float distanceSquaredTo( Point3f pos ) {
		return ( x - pos.x ) * ( x - pos.x ) + ( y - pos.y ) * ( y - pos.y )
		        + ( z - pos.z ) * ( z - pos.z );
	}

	public float distanceTo( Point3f pos ) {
		return (float) Math.sqrt(( x - pos.x ) * ( x - pos.x ) + ( y - pos.y )
		        * ( y - pos.y ) + ( z - pos.z ) * ( z - pos.z ));
	}
}
