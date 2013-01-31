package com.nputmedia.craftcraft.input;

import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.input.Keyboard;

public class KeyboardEventManager {
	private static final HashMap<Integer, ArrayList<Runnable>> keyUpListeners = new HashMap<Integer, ArrayList<Runnable>>();

	/**
	 * Should be called every logic tick to handle keyboard events.
	 */
	public static void update() {
		ArrayList<Runnable> listeners = null;
		// consume all the keyboard events
		while ( Keyboard.next() ) {
			// System.out.println(String.format("Key: %s(%d) Up=%s",
			// Keyboard.getEventCharacter(), key,
			// Keyboard.getEventKeyState()));

			// get the appropriate set of listeners for the current event type:
			// either up or down
			if ( Keyboard.getEventKeyState() ) {
				// keydown
			} else {
				listeners = keyUpListeners.get(Keyboard.getEventKey());
			}
			if ( listeners == null ) {
				continue;
			}

			// alert each listener
			for ( Runnable listener : listeners ) {
				listener.run();
			}
		}
	}

	/**
	 * Register a listener for a keyUp event.
	 * 
	 * @param key
	 *            the key to listen for
	 * @param listener
	 *            the callback object
	 */
	public static void addKeyUpListener( int key, Runnable listener ) {
		ArrayList<Runnable> listeners = keyUpListeners.get(key);
		if ( listeners == null ) {
			listeners = new ArrayList<Runnable>();
			keyUpListeners.put(key, listeners);
		}
		listeners.add(listener);
	}
}
