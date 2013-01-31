package com.nputmedia.craftcraft.graphics;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import com.nputmedia.craftcraft.graphics.primitives.Cone;
import com.nputmedia.craftcraft.graphics.primitives.Plane;
import com.nputmedia.craftcraft.graphics.primitives.Point3f;
import com.nputmedia.craftcraft.graphics.primitives.Sphere;
import com.nputmedia.craftcraft.logging.Logging;

/**
 * 
 * Camera
 * 
 * A basic camera class for setting the camera in a 3D scene.
 * 
 * Follows the interface laid out in: Computer Graphics Using OpenGL F.S. Hill, JR. Figure 7.10 page
 * 367 - The Camera class definition
 * 
 * @author Nathaniel Troutman
 * @version $Revision$ ($Author$)
 */
public class Camera {
	// A constant for converting degrees to radians
	protected static final float DEGREES_TO_RADIANS = (float) Math.PI / 180f;

	public static enum FRUSTRUM_CHECK {
		OUTSIDE, INSIDE, INTERSECT
	}

	// The camera's location
	public Point3f pos = new Point3f(0, 0, 0);

	// The vectors describing the cameras co-ordinate frame
	protected Point3f _u = new Point3f(0, 0, 0);

	public Point3f _v = new Point3f(0, 0, 0);

	public Point3f _n = new Point3f(0, 0, 0);

	// The view volume
	protected float _viewAngle = 0f, _aspect = 0f, _nearDist = 0f,
	        _farDist = 0f;

	protected float _height = 0;
	protected float _width = 0;

	private final Logging _log = Logging.createLogger(Camera.class);

	// Has the transformation matrix changed
	protected boolean _matrixChanged = true;

	public final Sphere frustrumSphere = new Sphere();
	public final Cone frustrumCone = new Cone();

	private final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

	public Plane[] frustrumPlanes;
	public ArrayList<Point3f[]> frustrumPoints = new ArrayList<Point3f[]>();

	public boolean lockN = false;

	/**
	 * 
	 * Builds this Camera object attaching the given GL to it. The gl will be used when setting the
	 * model view matrix.
	 * 
	 * @param gl
	 *            the gl to use
	 */
	public Camera() {
		_log.debug("Camera Made");

		frustrumPlanes = new Plane[6];
		for ( int i = 0; i < 6; i++ ) {
			frustrumPlanes[i] = new Plane();
		}
	}

	/**
	 * 
	 * Sets the camera up
	 * 
	 * @param eye
	 *            the position of the camera
	 * @param look
	 *            where the camera is looking
	 * @param up
	 *            the up vector of the camera
	 */
	public void set( Point3f eye, Point3f look, Point3f up ) {
		pos.set(eye);

		_n.set(eye.subtract(look));
		_n.normalizeLocal();

		_u.set(up.cross(_n));
		_u.normalizeLocal();

		_v.set(_n.cross(_u));
		_v.normalizeLocal();

		setModelViewMatrix();
	}

	/**
	 * 
	 * Roll the camera through the given angle
	 * 
	 * @param angle
	 *            the angle to row the camera
	 */
	public void roll( float angle ) {
		// Precalc the value of the sine and cosine
		float cos = (float) Math.cos(DEGREES_TO_RADIANS * angle);
		float sin = (float) Math.sin(DEGREES_TO_RADIANS * angle);

		// Temp copy of U
		Point3f t = new Point3f(_u);

		// Set up the vectors
		_u.set(cos * t.x - sin * _v.x, cos * t.y - sin * _v.y, cos * t.z - sin
		        * _v.z);
		_v.set(sin * t.x + cos * _v.x, sin * t.y + cos * _v.y, sin * t.z + cos
		        * _v.z);
		_v.normalizeLocal();
		_u.normalizeLocal();

		// Set the matrix
		setModelViewMatrix();
	}

	/**
	 * 
	 * Pitch the camera through the given angle
	 * 
	 * @param angle
	 *            the angle to pitch the camera
	 */
	public void pitch( float angle ) {
		// Precalc the sine and cosine
		float cos = (float) Math.cos(DEGREES_TO_RADIANS * angle);
		float sin = (float) Math.sin(DEGREES_TO_RADIANS * angle);

		// Temp copy of V
		Point3f t = new Point3f(_v);

		// Set the vectors
		_v.set(cos * t.x - sin * _n.x, cos * t.y - sin * _n.y, cos * t.z - sin
		        * _n.z);
		_n.set(sin * t.x + cos * _n.x, sin * t.y + cos * _n.y, sin * t.z + cos
		        * _n.z);

		// Update the matrix
		setModelViewMatrix();
	}

	/**
	 * 
	 * Yaw the camera through the given angle
	 * 
	 * @param angle
	 *            the angle to yaw the camera
	 */
	public void yaw( float angle ) {
		// Precalc the sine and cosine
		float cos = (float) Math.cos(DEGREES_TO_RADIANS * angle);
		float sin = (float) Math.sin(DEGREES_TO_RADIANS * angle);

		// Temp copy of U
		Point3f t = new Point3f(_u);

		// Set the vectors
		_u.set(cos * t.x - sin * _n.x, cos * t.y - sin * _n.y, cos * t.z - sin
		        * _n.z);
		_n.set(sin * t.x + cos * _n.x, sin * t.y + cos * _n.y, sin * t.z + cos
		        * _n.z);

		// Update the matrix
		setModelViewMatrix();
	}

	/**
	 * 
	 * Slide the camera around.
	 * 
	 * @param forward
	 *            the amount to slide the camera forwards or backwards if negative
	 * @param side
	 *            the amount to slide the camera from side to side
	 * @param elevate
	 *            the amount to elevate or depress the camera if negative
	 */
	public void slide( float forward, float side, float elevate ) {
		// Used for debugging
		Point3f oldEye = new Point3f(pos);

		// Recalc the eye's position
		pos.x += forward * _n.x + side * _u.x + elevate * _v.x;
		pos.y += forward * _n.y + side * _u.y + elevate * _v.y;
		pos.z += forward * _n.z + side * _u.z + elevate * _v.z;

		// Debug it
		if ( _log.isDebugEnabled() ) {
			_log.debug("Camera slide[" + forward + "," + side + "," + elevate
			        + "] OldEye=" + oldEye + " NewEye=" + pos);
		}

		// Update the matrix
		setModelViewMatrix();
	}

	/**
	 * 
	 * Set the shape of the viewing volume.
	 * 
	 * @param viewAngle
	 *            the field of view in degrees
	 * @param aspect
	 *            the aspect ratio of the volume
	 * @param nearDist
	 *            the distance of the near clipping plane
	 * @param farDist
	 *            the distance of the far clipping plane
	 */
	public void setShape( float viewAngle, float aspect, float nearDist,
	        float farDist ) {
		_viewAngle = viewAngle * DEGREES_TO_RADIANS;
		_aspect = aspect;
		_nearDist = nearDist;
		_farDist = farDist;

		_height = (float) ( Math.tan(_viewAngle * .5d) * _nearDist );
		_width = _height * _aspect;

		GLU.gluPerspective(viewAngle, _aspect, _nearDist, _farDist);
	}

	public float getWidth() {
		return _width;
	}

	public float getHeight() {
		return _height;
	}

	/**
	 * 
	 * Set the model view matrix of the given gl to that of this camera
	 * 
	 * @param gl
	 *            the gl of who's matrix to set.
	 */
	public void setModelViewMatrix() {
		if ( !lockN ) {
			lockedN = new Point3f(_n);
		}
		updateFrustrumBoundingVolumes();
		updateFrustrumPlanes();

		//
		// Create the new transformation matrix
		buffer.rewind();
		buffer.put(_u.x);
		buffer.put(_v.x);
		buffer.put(_n.x);
		buffer.put(0f);
		buffer.put(_u.y);
		buffer.put(_v.y);
		buffer.put(_n.y);
		buffer.put(0f);
		buffer.put(_u.z);
		buffer.put(_v.z);
		buffer.put(_n.z);
		buffer.put(0);
		buffer.put( -pos.dot(_u));
		buffer.put( -pos.dot(_v));
		buffer.put( -pos.dot(_n));
		buffer.put(1f);
		buffer.rewind();

		// Set the model view matrix
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadMatrix(buffer);
	}

	private void updateFrustrumBoundingVolumes() {
		//
		// Frustrum Sphere and Cone creation inspired by:
		// Dion Picco
		// http://www.flipcode.com/archives/Frustum_Culling.shtml
		//

		//
		// Update our frustum data

		// calculate the radius of the frustum sphere
		float fViewLen = _farDist - _nearDist;

		// use some trig to find the height of the frustum at the far plane
		float fHeight = (float) ( fViewLen * Math.tan(_viewAngle * 0.5f) );

		// with an aspect ratio of 1, the width will be the same
		float fWidth = _aspect * fHeight;

		// halfway point between near/far planes starting at the origin and
		// extending along the z axis
		Point3f P = new Point3f(0.0f, 0.0f, _nearDist + fViewLen * 0.5f);

		// the calculate far corner of the frustum
		Point3f Q = new Point3f(fWidth, fHeight, fViewLen);

		// the vector between P and Q
		Point3f vDiff = P.subtract(Q);

		// the radius becomes the length of this vector
		frustrumSphere.radius = vDiff.magnitude();

		// calculate the center of the sphere
		frustrumSphere.center = pos.add(_n.scale(( fViewLen * 0.5f )
		        + _nearDist));

		//
		// Calculate the frustum cone
		// calculate the length of the fov triangle
		float fDepth = (float) ( fHeight / Math.tan(_viewAngle * 0.5f) );

		// calculate the corner of the screen
		float fCorner = (float) Math.sqrt(fWidth * fWidth + fHeight * fHeight);

		// now calculate the new fov
		float fFov = (float) Math.atan(fCorner / fDepth);

		// apply to the cone
		frustrumCone.axis = _n;
		frustrumCone.vertex = pos;
		frustrumCone.angle = fFov;
	}

	public boolean lockFrustrum = false;

	public Point3f lockedN;

	private void updateFrustrumPlanes() {
		if ( lockFrustrum ) {
			return;
		}

		frustrumPoints.clear();

		Point3f localN = new Point3f(_n).scale( -1);
		Point3f nearCenter = localN.scale(_nearDist);
		nearCenter.addLocal(pos);
		frustrumPlanes[0].normal = localN;
		frustrumPlanes[0].point = nearCenter;

		Point3f farCenter = localN.scale(_farDist);
		farCenter.addLocal(pos);
		frustrumPlanes[1].normal = localN.scale( -1);
		frustrumPlanes[1].point = farCenter;

		float nearHeight = (float) ( 2 * Math.tan(_viewAngle * .5) * _nearDist );
		float nearWidth = nearHeight * _aspect;

		Point3f localPos = new Point3f(pos);
		Point3f scaledU = _u.scale(nearWidth * .5f);
		Point3f scaledV = _v.scale(nearHeight * .5f);
		Point3f vec = nearCenter.add(scaledU).addLocal(scaledV)
		        .subtractLocal(localPos);
		vec.normalizeLocal();

		frustrumPlanes[2].normal = _v.cross(vec).normalizeLocal();
		frustrumPlanes[2].point = localPos;

		frustrumPlanes[4].normal = vec.cross(_u).normalizeLocal();
		frustrumPlanes[4].point = localPos;

		vec = nearCenter.subtract(pos).subtractLocal(scaledU)
		        .subtractLocal(scaledV);
		vec.normalizeLocal();

		frustrumPlanes[3].normal = vec.cross(_v).normalizeLocal();
		frustrumPlanes[3].point = localPos;

		frustrumPlanes[5].normal = _u.cross(vec).normalizeLocal();
		frustrumPlanes[5].point = localPos;

		frustrumPoints.add(new Point3f[] { localPos, localPos.add(vec) });
		frustrumPoints.add(new Point3f[] {
		        nearCenter.add(scaledU).addLocal(scaledV),
		        nearCenter.subtract(scaledU).addLocal(scaledV) });
		frustrumPoints.add(new Point3f[] {
		        nearCenter.add(scaledU).addLocal(scaledV),
		        nearCenter.add(scaledU).subtractLocal(scaledV) });
		frustrumPoints.add(new Point3f[] {
		        nearCenter.subtract(scaledU).subtractLocal(scaledV),
		        nearCenter.subtract(scaledU).addLocal(scaledV) });
		frustrumPoints.add(new Point3f[] {
		        nearCenter.subtract(scaledU).subtractLocal(scaledV),
		        nearCenter.add(scaledU).subtractLocal(scaledV) });

		float farHeight = (float) ( 2 * Math.tan(_viewAngle * .5) * _farDist );
		float farWidth = farHeight * _aspect;

		localPos.set(pos);
		scaledU = _u.scale(farWidth * .5f);
		scaledV = _v.scale(farHeight * .5f);
		vec = farCenter.add(scaledU).subtractLocal(scaledV).addLocal(localPos);
		vec.normalizeLocal();
		frustrumPoints.add(new Point3f[] { localPos, localPos.add(vec) });
		frustrumPoints.add(new Point3f[] {
		        farCenter.add(scaledU).addLocal(scaledV),
		        farCenter.subtract(scaledU).addLocal(scaledV) });
		frustrumPoints.add(new Point3f[] {
		        farCenter.add(scaledU).addLocal(scaledV),
		        farCenter.add(scaledU).subtractLocal(scaledV) });
		frustrumPoints.add(new Point3f[] {
		        farCenter.subtract(scaledU).subtractLocal(scaledV),
		        farCenter.subtract(scaledU).addLocal(scaledV) });
		frustrumPoints.add(new Point3f[] {
		        farCenter.subtract(scaledU).subtractLocal(scaledV),
		        farCenter.add(scaledU).subtractLocal(scaledV) });

		frustrumPoints.add(new Point3f[] {
		        farCenter.add(scaledU).addLocal(scaledV), localPos });
		frustrumPoints.add(new Point3f[] {
		        farCenter.add(scaledU).subtractLocal(scaledV), localPos });
		frustrumPoints.add(new Point3f[] {
		        farCenter.subtract(scaledU).subtractLocal(scaledV), localPos });
		frustrumPoints.add(new Point3f[] {
		        farCenter.subtract(scaledU).addLocal(scaledV), localPos });

	}

	public FRUSTRUM_CHECK frustrumSphereCheck( Point3f center, float radius ) {
		// various distances
		float distance;

		// calculate our distances to each of the planes
		for ( int i = 0; i < 6; ++i ) {

			// find the distance to this plane
			distance = frustrumPlanes[i].distanceTo(center);

			// if this distance is < -sphere.radius, we are outside
			if ( distance < -radius ) {
				return ( FRUSTRUM_CHECK.OUTSIDE );
			}

			// else if the distance is between +- radius, then we intersect
			if ( Math.abs(distance) < radius ) {
				return ( FRUSTRUM_CHECK.INTERSECT );
			}
		}

		// otherwise we are fully in view
		return ( FRUSTRUM_CHECK.INSIDE );
	}

	public FRUSTRUM_CHECK frustumPointCheck( Point3f point ) {
		for ( int i = 0; i < 6; i++ ) {
			if ( frustrumPlanes[i].distanceTo(point) < 0 ) {
				return FRUSTRUM_CHECK.OUTSIDE;
			}
		}
		return FRUSTRUM_CHECK.INSIDE;
	}

	public FRUSTRUM_CHECK frustrumBoxCheck( Point3f corner, Point3f size ) {
		FRUSTRUM_CHECK result = FRUSTRUM_CHECK.INSIDE;
		int numOut = 0, numIn = 0;

		Point3f[] corners = { corner, corner.add(size.x, 0, 0),
		        corner.add(size.x, size.y, 0), corner.add(size.x, 0, size.y),
		        corner.add(size), corner.add(0, size.y, 0),
		        corner.add(0, size.y, size.z), corner.add(0, 0, size.z) };

		// for each plane do ...
		for ( int i = 0; i < 6; i++ ) {

			// reset counters for corners in and out
			numOut = 0;
			numIn = 0;

			// for each corner of the box do ...
			// get out of the cycle as soon as a box as corners
			// both inside and out of the frustum
			for ( int k = 0; k < 8 && ( numIn == 0 || numOut == 0 ); k++ ) {

				// is the corner outside or inside
				if ( frustrumPlanes[i].distanceTo(corners[k]) < 0 )
					numOut++ ;
				else
					numIn++ ;
			}
			// if all corners are out
			if ( numIn == 0 )
				return FRUSTRUM_CHECK.OUTSIDE;
			// if some corners are out and others are in
			else if ( numOut != 0 )
				result = FRUSTRUM_CHECK.INTERSECT;
		}
		return ( result );

	}

	public void renderFrustrum() {
		if ( !lockFrustrum )
			return;

		GL11.glPushAttrib(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBegin(GL11.GL_POINTS);
		// red, green, blue, yellow, magenta, cyan, white
		float[][] colors = { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 },
		        { 1, 1, 0 }, { 1, 0, 1 }, { 0, 1, 1 }, { 1, 1, 1 } };
		for ( int i = 0; i < 6; i++ ) {
			Point3f p = frustrumPlanes[i].point;
			Point3f n = frustrumPlanes[i].normal;
			GL11.glPointSize(10);
			GL11.glColor3f(colors[i][0], colors[i][1], colors[i][2]);
			GL11.glVertex3f(p.x, p.y, p.z);
			GL11.glPointSize(5);
			GL11.glVertex3f(p.x + n.x, p.y + n.y, p.z + n.z);
		}
		GL11.glEnd();

		GL11.glLineWidth(5);
		GL11.glBegin(GL11.GL_LINES);
		for ( int i = 0; i < 6; i++ ) {
			Point3f p = frustrumPlanes[i].point;
			Point3f n = frustrumPlanes[i].normal;
			GL11.glColor3f(colors[i][0], colors[i][1], colors[i][2]);
			GL11.glVertex3f(p.x, p.y, p.z);
			GL11.glColor3f(colors[i][0], colors[i][1], colors[i][2]);
			GL11.glVertex3f(p.x + n.x, p.y + n.y, p.z + n.z);
		}
		GL11.glColor3f(colors[6][0], colors[6][1], colors[6][2]);
		for ( Point3f[] points : frustrumPoints ) {
			GL11.glVertex3f(points[0].x, points[0].y, points[0].z);
			GL11.glVertex3f(points[1].x, points[1].y, points[1].z);
		}
		GL11.glEnd();
		GL11.glPopAttrib();
	}

	public void setUp( Point3f up ) {
		set(pos, _n, up);
	}
}
