package com.nputmedia.craftcraft.test;

import java.util.Random;

public class BytesVsInts {

	/**
	 * @param args
	 */
	public static void main( String[] args ) {
		// TODO Auto-generated method stub
		int NUM_TESTS = 10000;
		final int NUM_ELEMENTS = 2048;

		final byte[][] bytes = new byte[NUM_ELEMENTS][4];
		final int[] ints = new int[NUM_ELEMENTS];

		final Random gen = new Random(112358);

		Timer.timeit("Build Ints", NUM_TESTS, new Runnable() {
			public void run() {
				for ( int i = 0; i < NUM_ELEMENTS; i++ ) {
					ints[i] = gen.nextInt(256) << 24 | gen.nextInt(256) << 16
					        | gen.nextInt(256) << 8 | gen.nextInt(256);
				}
			}
		});

		gen.setSeed(112358);
		Timer.timeit("Build Bytes", NUM_TESTS, new Runnable() {
			public void run() {
				for ( int i = 0; i < NUM_ELEMENTS; i++ ) {
					bytes[i][0] = (byte) gen.nextInt(256);
					bytes[i][1] = (byte) gen.nextInt(256);
					bytes[i][2] = (byte) gen.nextInt(256);
					bytes[i][3] = (byte) gen.nextInt(256);
				}
			}
		});

		Timer.timeit("Read Ints", NUM_TESTS, new Runnable() {
			public void run() {
				for ( int i = 0; i < NUM_ELEMENTS; i++ ) {
					int n = ints[i];
					int b1 = ( n & 0xFF );
					int b2 = ( n >> 8 & 0xFF );
					int b3 = ( n >> 16 & 0xFF );
					int b4 = ( n >> 24 & 0xFF );
					int t = b1 + b2 + b3 + b4;
				}
			}
		});

		Timer.timeit("Read Bytes", NUM_TESTS, new Runnable() {
			public void run() {
				for ( int i = 0; i < NUM_ELEMENTS; i++ ) {
					byte b1 = bytes[i][0];
					byte b2 = bytes[i][1];
					byte b3 = bytes[i][2];
					byte b4 = bytes[i][3];
					int t = b1 + b2 + b3 + b4;
				}
			}
		});

	}
}
