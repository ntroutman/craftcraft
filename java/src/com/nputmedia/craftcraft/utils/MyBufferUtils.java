package com.nputmedia.craftcraft.utils;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

public class MyBufferUtils {
	public static FloatBuffer bufferFromArrayList( ArrayList<float[]> verts ) {
		int size = 0;
		if ( verts.size() != 0 ) {
			size = verts.size() * verts.get(0).length;
		}
		FloatBuffer vertBuffer = BufferUtils.createFloatBuffer(size);
		for ( float[] vert : verts ) {
			vertBuffer.put(vert);
		}
		return vertBuffer;
	}
}
