package com.nputmedia.craftcraft.graphics.primitives;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.nputmedia.craftcraft.graphics.FaceBuffers;

public class Box {
	public Point3f corner = null;
	public Point3f size = null;

	public Box() {
		corner = new Point3f();
		size = new Point3f();
	}

	public boolean containsPoint( Point3f point ) {
		return Box.containsPoint(corner, size, point);
	}

	public static boolean containsPoint( Point3f corner, Point3f size,
	        Point3f point ) {
		return ( corner.x < point.x && point.x < corner.x + size.x )
		        && ( corner.y < point.y && point.y < corner.y + size.y )
		        && ( corner.z < point.z && point.z < corner.z + size.z );
	}

	public static int renderBox( int faces, int x, int y, int z ) {
		GL11.glBegin(GL11.GL_QUADS);
		int renderedQuads = 0;
		// Back Face
		if ( ( faces & FaceBuffers.BACK_FACE_MASK ) != 0 ) {
			renderedQuads++ ;
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex3f(x, y, z + 1); // Bottom Left Of The Texture and Quad
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex3f(x + 1, y, z + 1); // Bottom Right Of The Texture and Quad
			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex3f(x + 1, y + 1, z + 1); // Top Right Of The Texture and Quad
			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex3f(x, y + 1, z + 1); // Top Left Of The Texture and Quad
		}

		// Front Face
		if ( ( faces & FaceBuffers.FRONT_FACE_MASK ) != 0 ) {
			renderedQuads++ ;
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex3f(x, y, z); // Bottom Right Of The Texture and Quad
			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex3f(x, y + 1, z); // Top Right Of The Texture and Quad
			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex3f(x + 1, y + 1, z); // Top Left Of The Texture and Quad
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex3f(x + 1, y, z); // Bottom Left Of The Texture and Quad
		}

		// Top Face
		if ( ( faces & FaceBuffers.TOP_FACE_MASK ) != 0 ) {
			renderedQuads++ ;
			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex3f(x, y + 1, z); // Top Left Of The Texture and Quad
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex3f(x, y + 1, z + 1); // Bottom Left Of The Texture and Quad
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex3f(x + 1, y + 1, z + 1); // Bottom Right Of The Texture and Quad
			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex3f(x + 1, y + 1, z); // Top Right Of The Texture and Quad
		}

		// Bottom Face
		if ( ( faces & FaceBuffers.BOTTOM_FACE_MASK ) != 0 ) {
			renderedQuads++ ;
			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex3f(x, y, z); // Top Right Of The Texture and Quad
			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex3f(x + 1, y, z); // Top Left Of The Texture and Quad
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex3f(x + 1, y, z + 1); // Bottom Left Of The Texture and Quad
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex3f(x, y, z + 1); // Bottom Right Of The Texture andQuad
		}

		// Right face
		if ( ( faces & FaceBuffers.RIGHT_FACE_MASK ) != 0 ) {
			renderedQuads++ ;
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex3f(x + 1, y, z); // Bottom Right Of The Texture andQuad
			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex3f(x + 1, y + 1, z); // Top Right Of The Texture and Quad
			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex3f(x + 1, y + 1, z + 1); // Top Left Of The Texture and Quad
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex3f(x + 1, y, z + 1); // Bottom Left Of The Texture and Quad
		}

		// Left Face
		if ( ( faces & FaceBuffers.LEFT_FACE_MASK ) != 0 ) {
			renderedQuads++ ;
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex3f(x, y, z); // Bottom Left Of The Texture and Quad
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex3f(x, y, z + 1); // Bottom Right Of The Texture and Quad
			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex3f(x, y + 1, z + 1); // Top Right Of The Texture and Quad
			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex3f(x, y + 1, z); // Top Left Of The Texture and Quad
		}
		GL11.glEnd();
		return renderedQuads;
	}

	public static void getBoxVertsAndTexCoords( FaceBuffers faceBuffers,
	        int faces, Point3f corner ) {
		getBoxVertsAndTexCoords(faceBuffers, faces, (int) corner.x,
		        (int) corner.y, (int) corner.y, 1, 1, 1);
	}

	public static void getBoxVertsAndTexCoords( FaceBuffers faceBuffers,
	        int faces, Point3f corner, Point3f size ) {
		getBoxVertsAndTexCoords(faceBuffers, faces, (int) corner.x,
		        (int) corner.y, (int) corner.y, (int) size.x, (int) size.y,
		        (int) size.z);
	}

	public static void getBoxVertsAndTexCoords( FaceBuffers faceBuffers,
	        int faces, int x, int y, int z ) {
		getBoxVertsAndTexCoords(faceBuffers, faces, x, y, z, 1, 1, 1);
	}

	public static void getBoxVertsAndTexCoords( FaceBuffers faceBuffers,
	        int faces, int x, int y, int z, int x_size, int y_size, int z_size ) {

		ArrayList<float[]> verts;
		ArrayList<float[]> texcoords;

		// Front Face
		if ( ( faces & FaceBuffers.FRONT_FACE_MASK ) != 0 ) {
			verts = faceBuffers.vertexPointMap.get(FaceBuffers.FRONT_FACE);
			verts.add(new float[] { x, y, z }); // Bottom Right Of The Texture and Quad
			verts.add(new float[] { x, y + 1, z }); // Top Right Of The Texture and Quad
			verts.add(new float[] { x + 1, y + 1, z }); // Top Left Of The Texture and Quad
			verts.add(new float[] { x + 1, y, z }); // Bottom Left Of The Texture and Quad

			texcoords = faceBuffers.texturePointMap.get(FaceBuffers.FRONT_FACE);
			texcoords.add(new float[] { 1.0f, 0.0f });
			texcoords.add(new float[] { 1.0f, 1.0f });
			texcoords.add(new float[] { 0.0f, 1.0f });
			texcoords.add(new float[] { 0.0f, 0.0f });
		}

		// Back Face
		if ( ( faces & FaceBuffers.BACK_FACE_MASK ) != 0 ) {
			verts = faceBuffers.vertexPointMap.get(FaceBuffers.BACK_FACE);
			verts.add(new float[] { x, y, z + 1 }); // Bottom Left Of The Texture and Quad
			verts.add(new float[] { x + 1, y, z + 1 }); // Bottom Right Of The Texture and Quad
			verts.add(new float[] { x + 1, y + 1, z + 1 }); // Top Right Of The Texture and Quad
			verts.add(new float[] { x, y + 1, z + 1 }); // Top Left Of The Texture and Quad

			texcoords = faceBuffers.texturePointMap.get(FaceBuffers.BACK_FACE);
			texcoords.add(new float[] { 0.0f, 0.0f });
			texcoords.add(new float[] { 1.0f, 0.0f });
			texcoords.add(new float[] { 1.0f, 1.0f });
			texcoords.add(new float[] { 0.0f, 1.0f });
		}

		// Top Face
		if ( ( faces & FaceBuffers.TOP_FACE_MASK ) != 0 ) {
			verts = faceBuffers.vertexPointMap.get(FaceBuffers.TOP_FACE);
			verts.add(new float[] { x, y + 1, z }); // Top Left Of The Texture and Quad
			verts.add(new float[] { x, y + 1, z + 1 }); // Bottom Left Of The Texture and Quad
			verts.add(new float[] { x + 1, y + 1, z + 1 }); // Bottom Right Of The Texture and Quad
			verts.add(new float[] { x + 1, y + 1, z }); // Top Right Of The Texture and Quad

			texcoords = faceBuffers.texturePointMap.get(FaceBuffers.TOP_FACE);
			texcoords.add(new float[] { 0.0f, 1.0f });
			texcoords.add(new float[] { 0.0f, 0.0f });
			texcoords.add(new float[] { 1.0f, 0.0f });
			texcoords.add(new float[] { 1.0f, 1.0f });
		}

		// Bottom Face
		if ( ( faces & FaceBuffers.BOTTOM_FACE_MASK ) != 0 ) {
			verts = faceBuffers.vertexPointMap.get(FaceBuffers.BOTTOM_FACE);
			verts.add(new float[] { x, y, z }); // Top Right Of The Texture and Quad
			verts.add(new float[] { x + 1, y, z }); // Top Left Of The Texture and Quad
			verts.add(new float[] { x + 1, y, z + 1 }); // Bottom Left Of The Texture and Quad
			verts.add(new float[] { x, y, z + 1 }); // Bottom Right Of The Texture andQuad

			texcoords = faceBuffers.texturePointMap
			        .get(FaceBuffers.BOTTOM_FACE);
			texcoords.add(new float[] { 1.0f, 1.0f });
			texcoords.add(new float[] { 0.0f, 1.0f });
			texcoords.add(new float[] { 0.0f, 0.0f });
			texcoords.add(new float[] { 1.0f, 0.0f });
		}

		// Right face
		if ( ( faces & FaceBuffers.RIGHT_FACE_MASK ) != 0 ) {
			verts = faceBuffers.vertexPointMap.get(FaceBuffers.RIGHT_FACE);
			verts.add(new float[] { x + 1, y, z }); // Bottom Right Of The Texture andQuad
			verts.add(new float[] { x + 1, y + 1, z }); // Top Right Of The Texture and Quad
			verts.add(new float[] { x + 1, y + 1, z + 1 }); // Top Left Of The Texture and Quad
			verts.add(new float[] { x + 1, y, z + 1 }); // Bottom Left Of The Texture and Quad

			texcoords = faceBuffers.texturePointMap.get(FaceBuffers.RIGHT_FACE);
			texcoords.add(new float[] { 1.0f, 0.0f });
			texcoords.add(new float[] { 1.0f, 1.0f });
			texcoords.add(new float[] { 0.0f, 1.0f });
			texcoords.add(new float[] { 0.0f, 0.0f });
		}

		// Left Face
		if ( ( faces & FaceBuffers.LEFT_FACE_MASK ) != 0 ) {
			verts = faceBuffers.vertexPointMap.get(FaceBuffers.LEFT_FACE);
			verts.add(new float[] { x, y, z }); // Bottom Left Of The Texture and Quad
			verts.add(new float[] { x, y, z + 1 }); // Bottom Right Of The Texture and Quad
			verts.add(new float[] { x, y + 1, z + 1 }); // Top Right Of The Texture and Quad
			verts.add(new float[] { x, y + 1, z }); // Top Left Of The Texture and Quad

			texcoords = faceBuffers.texturePointMap.get(FaceBuffers.LEFT_FACE);
			texcoords.add(new float[] { 0.0f, 0.0f });
			texcoords.add(new float[] { 1.0f, 0.0f });
			texcoords.add(new float[] { 1.0f, 1.0f });
			texcoords.add(new float[] { 0.0f, 1.0f });
		}
	}
}
