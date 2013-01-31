package com.nputmedia.craftcraft;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import com.nputmedia.craftcraft.controllers.CameraController;
import com.nputmedia.craftcraft.graphics.Camera;
import com.nputmedia.craftcraft.graphics.OctTreeNode;
import com.nputmedia.craftcraft.graphics.primitives.Point3f;
import com.nputmedia.craftcraft.input.KeyboardEventManager;
import com.nputmedia.craftcraft.world.Chunk;

/**
 * Basic game
 * 
 * @author Name <email>
 * @version 1.0
 */
public class CraftCraft {
	private static final String CHUNK_FILE = "/z/random_large.chunk";
	private boolean done = false;
	private boolean fullscreen = false;
	private final String windowTitle = "NeHe's OpenGL Lesson 3 for LWJGL (Adding Color)";
	private final boolean f1 = false;
	private DisplayMode displayMode;
	private final Queue<Float> stats = new LinkedList<Float>();

	// Number of nano seconds to run at 60 ticks per second
	private static long TPS_60 = 16666667;
	private static long TPS_2 = 500000000000l;

	// Field of view for perspective
	protected static final float FOV = 45f;

	// Near clipping distance
	protected static final float NEAR_DIST = .1f;

	// Far clipping distance
	protected static final float FAR_DIST = 100f;
	private static final float STATS_BUFFER_SIZE = 120;

	private Camera camera;
	private CameraController cameraController;
	private Chunk chunk;
	private ArrayList<OctTreeNode> renderList;

	public static void main( String args[] ) {
		boolean fullscreen = false;
		if ( args.length > 0 ) {
			if ( args[0].equalsIgnoreCase("fullscreen") ) {
				fullscreen = true;
			}
		}

		CraftCraft l3 = new CraftCraft();
		l3.run(fullscreen);
	}

	public CraftCraft() {
		// cameraController.disableRoll();
		// cameraController.disablePitch();
	}

	public void run( boolean fullscreen ) {
		this.fullscreen = fullscreen;
		try {
			init();
			long frames = 0;
			long fps = 0;
			long old_time = System.nanoTime(), cur_time = 0, elapsed_time = 0;
			while ( !done ) {
				cur_time = System.nanoTime();
				elapsed_time = cur_time - old_time;
				frames += 1;
				// Limit game logic to 60 FPS
				if ( elapsed_time > TPS_60 ) {
					old_time = cur_time;
					fps = ( frames * 1000000000l ) / ( elapsed_time );

					if ( stats.size() == STATS_BUFFER_SIZE ) {
						stats.poll();
					}
					stats.add((float) fps);

					// System.out.println("FPS: " + fps);
					frames = 0;
					mainloop();
					Display.setTitle("FPS: " + fps + " - "
					        + Properties.getRenderAndCullingString());
				}

				// Graphics updating isn't rate limited
				render();
				renderStats();
				Display.update();
			}
			cleanup();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void renderStats() {
		DisplayMode dm = Display.getDisplayMode();

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		GL11.glMatrixMode(GL11.GL_PROJECTION_MATRIX);

		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		int w = dm.getWidth();
		int h = dm.getHeight();
		GL11.glOrtho(0, w, 0, h, -1, 1);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		// GL11.glTranslatef(dm.getWidth() / 2, dm.getHeight() / 2, .5f);
		// GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glTranslatef( -.5f, -.4f, -1f);
		GL11.glScaled(1. / w, 1. / h, 1.);

		int max_y = 55;
		GL11.glColor3f(1f, 1f, 1f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(0, 0);
		GL11.glVertex2f(STATS_BUFFER_SIZE, 0);
		GL11.glVertex2f(STATS_BUFFER_SIZE, max_y);
		GL11.glVertex2f(0, max_y);
		GL11.glEnd();

		float x = 0;
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3f(1f, 0f, 0f);
		GL11.glLineWidth(3f);
		float mean = 0;
		for ( float y : stats ) {
			y /= 10f;
			mean += y;
			GL11.glVertex2f(x, 0);
			GL11.glVertex2f(x, y);
			x += 1;
		}
		mean /= x;
		GL11.glColor3f(0f, 1f, 0f);
		GL11.glVertex2f(0, mean);
		GL11.glVertex2f(x, mean);
		GL11.glColor3f(1f, 1f, 1f);
		GL11.glEnd();
		GL11.glPopMatrix();

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private void mainloop() {
		// Exit if Escape is pressed
		if ( Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) ) {
			done = true;
		}
		// Exit if window is closed
		if ( Display.isCloseRequested() ) {
			done = true;
		}
		KeyboardEventManager.update();
		cameraController.handleKeys();
	}

	private void switchMode() {
		fullscreen = !fullscreen;
		try {
			Display.setFullscreen(fullscreen);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean render() {
		// Clear The Screen And The Depth Buffer
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		camera.setModelViewMatrix();

		GL11.glEnable(GL11.GL_TEXTURE_2D);

		Properties.resetRenderAndCullingCounters();
		renderList.clear();
		chunk.renderChunk(camera, renderList);
		OctTreeNode.renderNodes(camera, renderList);
		camera.renderFrustrum();
		return true;
	}

	private void createWindow() throws Exception {
		Display.setFullscreen(fullscreen);
		DisplayMode d[] = Display.getAvailableDisplayModes();
		for ( int i = 0; i < d.length; i++ ) {
			if ( d[i].getWidth() == 640 && d[i].getHeight() == 480
			        && d[i].getBitsPerPixel() == 32 ) {
				displayMode = d[i];
				break;
			}
		}
		Display.setDisplayMode(displayMode);
		Display.setTitle(windowTitle);
		Display.create();
	}

	private void init() throws Exception {
		createWindow();

		camera = new Camera();
		cameraController = new CameraController(camera);

		KeyboardEventManager.addKeyUpListener(Keyboard.KEY_L, new Runnable() {
			public void run() {
				camera.lockFrustrum = !camera.lockFrustrum;
				System.out.println("Lock Frustrum:" + camera.lockFrustrum);
				camera.lockN = !camera.lockN;
			}
		});

		KeyboardEventManager.addKeyUpListener(Keyboard.KEY_O, new Runnable() {
			public void run() {
				Properties.wireframe = !Properties.wireframe;
			}
		});

		KeyboardEventManager.addKeyUpListener(Keyboard.KEY_G, new Runnable() {
			public void run() {
				Properties.physics = !Properties.physics;
				if ( Properties.physics ) {
					camera.setUp(Point3f.UNIT_Y);
					cameraController.disableRoll();
				}
			}
		});

		KeyboardEventManager.addKeyUpListener(Keyboard.KEY_X, new Runnable() {
			public void run() {
				Properties.doBackFaceCulling = !Properties.doBackFaceCulling;
			}
		});

		KeyboardEventManager.addKeyUpListener(Keyboard.KEY_C, new Runnable() {
			public void run() {
				System.out.println("Camera: pos=" + camera.pos + ", look="
				        + camera._n + ",  up=" + camera._v);
			}
		});

		renderList = new ArrayList<OctTreeNode>(32 * 32 * 32);
		chunk = new Chunk();
		chunk.load(CHUNK_FILE);
		initGL();
	}

	private void initGL() {
		// Face culling
		// GL11.glEnable(GL11.GL_CULL_FACE);
		// GL11.glFrontFace(GL11.GL_CCW);

		// Texturing
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);

		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Black Background
		GL11.glClearDepth(1.0); // Depth Buffer Setup

		GL11.glEnable(GL11.GL_DEPTH_TEST); // Enables Depth Testing
		GL11.glDepthFunc(GL11.GL_LEQUAL); // The Type Of Depth Testing To Do

		GL11.glMatrixMode(GL11.GL_PROJECTION); // Select The Projection Matrix
		GL11.glLoadIdentity(); // Reset The Projection Matrix

		// Calculate The Aspect Ratio Of The Window
		double _aspect = (double) Display.getDisplayMode().getWidth()
		        / (double) Display.getDisplayMode().getHeight();
		camera.setShape(FOV, (float) _aspect, NEAR_DIST, FAR_DIST);

		GL11.glMatrixMode(GL11.GL_MODELVIEW); // Select The Modelview Matrix

		// Really Nice Perspective Calculations
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);

		// camera.set(new Point3f(3.0f, 2.0f, 2.0f), new Point3f(6, 4, 6),
		// Point3f.UNIT_Y);
		// camera.set(new Point3f(0.0f, 0.0f, -5.f), new Point3f(0, 0, 0),
		// Point3f.UNIT_Y);
		camera.set(new Point3f(2.3f, 23.1f, -24.f), new Point3f(2.3f, 23.1f,
		        -24.f).subtract(new Point3f( -.37f, .16f, -.91f)), new Point3f(
		        0.043871f, 0.965729f, 0.255818f));
		Chunk.loadTextures();
	}

	private void cleanup() {
		Display.destroy();
	}

}