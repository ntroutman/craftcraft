package com.nputmedia.craftcraft.graphics;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import com.nputmedia.craftcraft.graphics.primitives.Point3f;
import com.nputmedia.craftcraft.utils.MyBufferUtils;

public class FaceBuffers {
	public static final Point3f[] FACE_NORMALS = { Point3f.UNIT_Z.scale( -1),
	        Point3f.UNIT_Z, Point3f.UNIT_Y, Point3f.UNIT_Y.scale( -1),
	        Point3f.UNIT_X.scale( -1), Point3f.UNIT_X };

	public static final int FRONT_FACE_MASK = 1;
	public static final int BACK_FACE_MASK = 1 << 1;
	public static final int TOP_FACE_MASK = 1 << 2;
	public static final int BOTTOM_FACE_MASK = 1 << 3;
	public static final int LEFT_FACE_MASK = 1 << 4;
	public static final int RIGHT_FACE_MASK = 1 << 5;

	public static final int FRONT_FACE = 0;
	public static final int BACK_FACE = 1;
	public static final int TOP_FACE = 2;
	public static final int BOTTOM_FACE = 3;
	public static final int LEFT_FACE = 4;
	public static final int RIGHT_FACE = 5;

	public final HashMap<Integer, FloatBuffer> vertexMap;
	public final HashMap<Integer, FloatBuffer> textureMap;

	public final HashMap<Integer, ArrayList<float[]>> vertexPointMap;
	public final HashMap<Integer, ArrayList<float[]>> texturePointMap;

	public FaceBuffers() {
		vertexMap = new HashMap<Integer, FloatBuffer>();
		textureMap = new HashMap<Integer, FloatBuffer>();

		vertexPointMap = new HashMap<Integer, ArrayList<float[]>>();
		texturePointMap = new HashMap<Integer, ArrayList<float[]>>();

		for ( int faceDirIdx = 0; faceDirIdx < FACE_NORMALS.length; faceDirIdx++ ) {
			vertexPointMap.put(faceDirIdx, new ArrayList<float[]>());
			texturePointMap.put(faceDirIdx, new ArrayList<float[]>());
		}

	}

	public void convertToBuffers() {
		for ( int faceDirIdx = 0; faceDirIdx < FACE_NORMALS.length; faceDirIdx++ ) {
			vertexMap.put(faceDirIdx, MyBufferUtils
			        .bufferFromArrayList(vertexPointMap.get(faceDirIdx)));
			textureMap.put(faceDirIdx, MyBufferUtils
			        .bufferFromArrayList(texturePointMap.get(faceDirIdx)));
		}
		vertexPointMap.clear();
		texturePointMap.clear();
	}
}
