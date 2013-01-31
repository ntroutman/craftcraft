package com.nputmedia.craftcraft.controllers;

import org.lwjgl.input.Keyboard;

import com.nputmedia.craftcraft.Properties;
import com.nputmedia.craftcraft.graphics.Camera;
import com.nputmedia.craftcraft.graphics.primitives.Point3f;
import com.nputmedia.craftcraft.world.Chunk;

/**
 * 
 * CameraController
 * 
 * A key listener that can be attached to a Swing compnent, such as a GLCanvas, to control a Camera
 * object.
 * 
 * @author Nathaniel Troutman
 * @version $Revision$ ($Author$)
 */
public class PlayerController {
	// Movement constants
	public static float FORWARD_STEP = -.05f;

	public static float STRAFE_STEP = -.05f;

	public static float ROLL_STEP = -.5f;

	public static float YAW_STEP = 1.0f;

	public static float PITCH_STEP = -.8f;

	public static float ELEVATE_STEP = .05f;

	// The camera we are controlling
	private Camera _cam = null;

	private boolean doPitch = true;

	private boolean doRoll = true;

	private boolean doYaw = true;

	/**
	 * 
	 * Builds this CamerController object
	 * 
	 * @param camera
	 *            The camera to control
	 */
	public PlayerController( Camera camera ) {
		_cam = camera;
	}

	public void handleKeys( Chunk curChunk ) {
		Point3f old_pos = new Point3f(_cam.pos);

		if ( Keyboard.isKeyDown(Keyboard.KEY_W) )
			_cam.slide(FORWARD_STEP, 0, 0);
		else if ( Keyboard.isKeyDown(Keyboard.KEY_S) )
			_cam.slide( -FORWARD_STEP, 0, 0);

		if ( Keyboard.isKeyDown(Keyboard.KEY_A) )
			_cam.slide(0, STRAFE_STEP, 0);
		else if ( Keyboard.isKeyDown(Keyboard.KEY_D) )
			_cam.slide(0, -STRAFE_STEP, 0);

		if ( Properties.physics ) {
			int x = (int) _cam.pos.x;
			int y = (int) _cam.pos.y;
			int z = (int) _cam.pos.z;
			if ( curChunk.data[x][y][z] != Chunk.BLOCK_TYPE_EMPTY ) {
				_cam.set(_cam.pos, _cam._n, _cam._v);
			}
		}

		if ( !Properties.physics ) {
			if ( Keyboard.isKeyDown(Keyboard.KEY_R) )
				_cam.slide(0, 0, ELEVATE_STEP);
			else if ( Keyboard.isKeyDown(Keyboard.KEY_F) )
				_cam.slide(0, 0, -ELEVATE_STEP);
		}

		if ( doYaw ) {
			if ( Keyboard.isKeyDown(Keyboard.KEY_LEFT) )
				_cam.yaw(YAW_STEP);
			else if ( Keyboard.isKeyDown(Keyboard.KEY_RIGHT) )
				_cam.yaw( -YAW_STEP);
		}

		if ( doPitch ) {
			if ( Keyboard.isKeyDown(Keyboard.KEY_UP) )
				_cam.pitch(PITCH_STEP);
			else if ( Keyboard.isKeyDown(Keyboard.KEY_DOWN) )
				_cam.pitch( -PITCH_STEP);
		}

	}

	public void update( Chunk curChunk ) {
		if ( !Properties.physics )
			return;

		int x = (int) _cam.pos.x;
		int y = (int) _cam.pos.y;
		int z = (int) _cam.pos.z;
		if ( y > 0 && curChunk.data[x][y - 1][z] == Chunk.BLOCK_TYPE_EMPTY ) {
			_cam.slide(0, 0, (float) Math.min( -.1, -( _cam.pos.z - z )));
		}
	}

	public void disableRoll() {
		doRoll = false;
	}

	public void enableRoll() {
		doRoll = true;
	}

	public void disablePitch() {
		doPitch = false;
	}

	public void enablePitch() {
		doPitch = true;
	}

	public void disableYaw() {
		doYaw = false;
	}

	public void enableYaw() {
		doYaw = true;
	}

}
