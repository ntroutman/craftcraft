package com.nputmedia.craftcraft.test;

public class Timer {
	private String name;
	private long time;

	public Timer() {
	}

	public static void timeit( String name, int numRuns, Runnable r ) {
		// Let the JIT work a bit
		for ( int i = 0; i < 100; i++ ) {
			r.run();
		}

		long start = System.currentTimeMillis();
		for ( int i = 0; i < numRuns; i++ ) {
			r.run();
		}
		long end = System.currentTimeMillis();
		System.out.println(name + ": " + ( end - start ));
	}

	public void start( String name ) {
		this.name = name;
		time = System.currentTimeMillis();
	}

	public void stop() {
		time = System.currentTimeMillis() - time;
		System.out.println(name + ": " + time);
	}
}
