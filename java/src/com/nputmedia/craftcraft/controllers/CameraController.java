package com.nputmedia.craftcraft.controllers;

import org.lwjgl.input.Keyboard;

import com.nputmedia.craftcraft.graphics.Camera;

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
public class CameraController {
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
	public CameraController( Camera camera ) {
		_cam = camera;
	}

	public void handleKeys() {
		if ( Keyboard.isKeyDown(Keyboard.KEY_W) )
			_cam.slide(FORWARD_STEP, 0, 0);
		else if ( Keyboard.isKeyDown(Keyboard.KEY_S) )
			_cam.slide( -FORWARD_STEP, 0, 0);

		if ( Keyboard.isKeyDown(Keyboard.KEY_A) )
			_cam.slide(0, STRAFE_STEP, 0);
		else if ( Keyboard.isKeyDown(Keyboard.KEY_D) )
			_cam.slide(0, -STRAFE_STEP, 0);

		if ( Keyboard.isKeyDown(Keyboard.KEY_R) )
			_cam.slide(0, 0, ELEVATE_STEP);
		else if ( Keyboard.isKeyDown(Keyboard.KEY_F) )
			_cam.slide(0, 0, -ELEVATE_STEP);

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

		if ( doRoll ) {
			if ( Keyboard.isKeyDown(Keyboard.KEY_Q) )
				_cam.roll(ROLL_STEP);
			else if ( Keyboard.isKeyDown(Keyboard.KEY_E) )
				_cam.roll( -ROLL_STEP);
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
