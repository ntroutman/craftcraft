package com.nputmedia.craftcraft.world;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.lwjgl.opengl.GL11;

import com.nputmedia.craftcraft.graphics.Camera;
import com.nputmedia.craftcraft.graphics.Camera.FRUSTRUM_CHECK;
import com.nputmedia.craftcraft.graphics.Texture;
import com.nputmedia.craftcraft.graphics.primitives.Box;
import com.nputmedia.craftcraft.graphics.primitives.Point3f;
import com.nputmedia.craftcraft.graphics.primitives.Sphere;

public class OldChunk {
	private static final int BACK_FACE = 1;
	private static final int FRONT_FACE = 1 << 1;
	private static final int TOP_FACE = 1 << 2;
	private static final int BOTTOM_FACE = 1 << 3;
	private static final int LEFT_FACE = 1 << 4;
	private static final int RIGHT_FACE = 1 << 5;

	/**
	 * block_type is the shape
	 * 
	 * block_faces is which faces are visible
	 * 
	 * block_texture is which texture to draw
	 * 
	 * block_subtexture is use for blocks that can have multiple textures, it specifies which
	 * sub_texture to use
	 * 
	 * block_rotation is which of the 8 directions the block is rotated
	 */

	public static final int BLOCK_TYPE_EMPTY = 0;
	public static final int BLOCK_TYPE_SOLID = 1;
	public static final int BLOCK_TYPE_SLOPE = 2;
	public static final int BLOCK_TYPE_CORNER = 3;
	public static final int BLOCK_TYPE_JOINT = 4;

	public static final int BLOCK_TEXTURE_SOIL = 1;
	public static final int BLOCK_TEXTURE_STONE = 2;

	private int x_size;
	private int y_size;
	private int z_size;
	private int[][][] data;

	private Box bbox = null;
	private Sphere bsphere = null;
	private int culled = 0;
	private int rendered = 0;
	private int renderedQuads = 0;
	private static Texture[] textures;

	public OldChunk() {
		bbox = new Box();
		bsphere = new Sphere();
	}

	public static void loadTextures() {
		textures = new Texture[3];
		textures[1] = new Texture("/images/grass.png");
	}

	public void setData( int[][][] newData ) {
		x_size = newData.length;
		y_size = newData[0].length;
		z_size = newData[0][0].length;
		data = newData;
		updateBounds();
		fixFaceVisibility();
	}

	private void updateBounds() {
		bbox.size.x = x_size;
		bbox.size.y = y_size;
		bbox.size.z = z_size;

		bsphere.center.x = bbox.corner.x + ( bbox.size.x / 2 );
		bsphere.center.y = bbox.corner.y + ( bbox.size.y / 2 );
		bsphere.center.z = bbox.corner.z + ( bbox.size.z / 2 );

		bsphere.radius = bsphere.center.subtract(bbox.corner).magnitude();
	}

	public void load( String filename ) {
		try {
			System.out.println("Loading: " + filename);
			load(new DataInputStream(new FileInputStream(filename)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void load( DataInputStream chunkStream ) {
		try {
			x_size = chunkStream.readInt();
			y_size = chunkStream.readInt();
			z_size = chunkStream.readInt();

			System.out.println(String.format("loading chunk: x=%d, y=%d, z=%d",
			        x_size, y_size, z_size));

			int block_type, face, subtexture, texture, rotation, d;
			int solid = 0, empty = 0;
			data = new int[x_size][y_size][z_size];
			for ( int ix = 0; ix < x_size; ix++ ) {
				for ( int iy = 0; iy < y_size; iy++ ) {
					for ( int iz = 0; iz < z_size; iz++ ) {
						d = chunkStream.readInt();
						data[ix][iy][iz] = d;

						block_type = d >> 29; // 3 bits
						rotation = d >> 24 & 0x1f; // 5 bits
						face = d >> 16 & 0xFF; // 8 bits
						texture = d >> 8 & 0xFF; // 8 bits
						subtexture = d & 0xFF; // 8 bits

						if ( block_type != BLOCK_TYPE_EMPTY ) {
							solid++ ;
						} else {
							empty++ ;
						}
					}
				}
			}
			System.out.println("\tsolid=" + solid);
			System.out.println("\tempty=" + empty);
			System.out.println("\ttotal=" + ( solid + empty ));
			setData(data);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void save( String filename ) {
		try {
			System.out.println("Saving: " + filename);
			save(new DataOutputStream(new FileOutputStream(filename)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void save( DataOutputStream chunkStream ) {
		try {
			chunkStream.writeInt(x_size);
			chunkStream.writeInt(y_size);
			chunkStream.writeInt(z_size);

			for ( int ix = 0; ix < x_size; ix++ ) {
				for ( int iy = 0; iy < y_size; iy++ ) {
					for ( int iz = 0; iz < z_size; iz++ ) {
						chunkStream.writeInt(data[ix][iy][iz]);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Splits an segment into the pieces of: type, rotation, face, texture, subtexture.
	 * 
	 * 
	 * @param segment
	 *            the segement to split
	 * @param pieces
	 *            the array to place the pieces into, must be size 5. if null then an class variable
	 *            array is returned.
	 * 
	 * @return the array passed into, or the class variable
	 */
	private static final int[] _segmentBuffer = new int[5];

	private final int[] splitSegment( int segment, int[] pieces ) {
		if ( pieces == null ) {
			pieces = _segmentBuffer;
		}

		return pieces;
	}

	public void renderChunk( Camera camera ) {
		// Point3f pos = camera.pos;
		culled = 0;
		rendered = 0;
		renderedQuads = 0;
		renderChunk(camera, bbox.corner, bbox.size);
		System.out.println("culled: " + culled + " rendered: " + rendered
		        + " quads: " + renderedQuads + "/" + rendered * 6);

	}

	/**
	 * This renders the sub piece of the cunk starting at corner of size units. The chunk is culled
	 * against the camera frustrum by viewing it as an octtree.
	 * 
	 * @param camera
	 * @param corner
	 * @param size
	 */
	private void renderChunk( Camera camera, Point3f corner, Point3f size ) {
		Point3f center = size.scale(.5f).addLocal(corner);
		float radius = size.magnitude() * .5f;
		// test the bounding sphere first
		FRUSTRUM_CHECK check = camera.frustrumSphereCheck(center, radius);
		switch ( check ) {
		case OUTSIDE:
			culled += countNonEmpty(corner, size);
			return;
		case INSIDE:
			break;
		case INTERSECT:
			// check if the bound box is in view
			check = camera.frustrumBoxCheck(corner, size);
			switch ( check ) {
			case INSIDE:
				break;
			case OUTSIDE:
				culled += countNonEmpty(corner, size);
				return;
			}
			break;
		}
		// If the size is greater 4 in any dimension recursively call renderChunk
		// on each octant
		if ( size.x > 4 || size.y > 4 || size.z > 4 ) {
			Point3f new_size = size.scale(.5f);
			Point3f[] corners = { corner, corner.add(new_size.x, 0, 0),
			        corner.add(new_size.x, new_size.y, 0),
			        corner.add(new_size.x, 0, new_size.y),
			        corner.add(new_size), corner.add(0, new_size.y, 0),
			        corner.add(0, new_size.y, new_size.z),
			        corner.add(0, 0, new_size.z) };

			for ( Point3f new_corner : corners ) {
				renderChunk(camera, new_corner, new_size);
			}
		} else {
			// The sub-region of the chunk is now small enough for rendering

			int block_type, faces, subtexture, texture, rotation, d;
			int max_x = (int) ( corner.x + size.x );
			int max_y = (int) ( corner.y + size.y );
			int max_z = (int) ( corner.z + size.z );

			// Render each block of the sub-region
			for ( int x = (int) corner.x; x < max_x; x++ ) {
				for ( int y = (int) corner.y; y < max_y; y++ ) {
					for ( int z = (int) corner.z; z < max_z; z++ ) {
						d = data[x][y][z];

						block_type = d >> 29; // 3 bits
						rotation = d >> 24 & 0x1f; // 5 bits
						faces = d >> 16 & 0xFF; // 8 bits
						texture = d >> 8 & 0xFF; // 8 bits
						subtexture = d & 0xFF; // 8 bits

						// only render blocks which have visible faces
						if ( faces != 0 ) {
							textures[texture].bind();
							rendered += 1;
							switch ( block_type ) {
							case BLOCK_TYPE_SOLID:
								renderedQuads += renderBox(faces, x, y, z);
								break;
							case BLOCK_TYPE_SLOPE:

								break;
							case BLOCK_TYPE_JOINT:

								break;
							}
						}
					}
				}
			}
		}

	}

	private int countNonEmpty( Point3f corner, Point3f size ) {
		int block_type, d, solid = 0;
		int max_x = (int) ( corner.x + size.x );
		int max_y = (int) ( corner.y + size.y );
		int max_z = (int) ( corner.z + size.z );
		for ( int x = (int) corner.x; x < max_x; x++ ) {
			for ( int y = (int) corner.y; y < max_y; y++ ) {
				for ( int z = (int) corner.z; z < max_z; z++ ) {
					d = data[x][y][z];

					block_type = d >> 24;

					if ( block_type != BLOCK_TYPE_EMPTY ) {
						solid += 1;
					}
				}
			}
		}
		return solid;
	}

	/**
	 * Renders a cube at (x,y,z) and only draws the faces indicated.
	 * 
	 * @param faces
	 *            The faces to draw of the cube
	 * @param x
	 *            x-position
	 * @param y
	 *            y-position
	 * @param z
	 *            z-position
	 * @return number of faces/quads drawn
	 */
	public static int renderBox( int faces, int x, int y, int z ) {
		GL11.glBegin(GL11.GL_QUADS);
		int renderedQuads = 0;
		// Back Face
		if ( ( faces & BACK_FACE ) != 0 ) {
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
		if ( ( faces & FRONT_FACE ) != 0 ) {
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
		if ( ( faces & TOP_FACE ) != 0 ) {
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
		if ( ( faces & BOTTOM_FACE ) != 0 ) {
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
		if ( ( faces & RIGHT_FACE ) != 0 ) {
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
		if ( ( faces & LEFT_FACE ) != 0 ) {
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

	/**
	 * Checks each block and determines which faces are visible. By checking the size neighboring
	 * blocks to see if they are empty or not.
	 */
	private void fixFaceVisibility() {
		int block_type, faces, showing = 0, solid = 0;
		for ( int ix = 0; ix < x_size; ix++ ) {
			for ( int iy = 0; iy < y_size; iy++ ) {
				for ( int iz = 0; iz < z_size; iz++ ) {
					block_type = getBlockType(ix, iy, iz);
					if ( block_type == BLOCK_TYPE_EMPTY )
						continue;
					solid++ ;
					faces = 0;
					if ( ix > 0 && getBlockType(ix - 1, iy, iz) == 0 ) {
						faces |= LEFT_FACE;
						showing++ ;
					}
					if ( ix + 1 < x_size && getBlockType(ix + 1, iy, iz) == 0 ) {
						faces |= RIGHT_FACE;
						showing++ ;
					}
					if ( iy > 0 && getBlockType(ix, iy - 1, iz) == 0 ) {
						faces |= BOTTOM_FACE;
						showing++ ;
					}
					if ( iy + 1 < y_size && getBlockType(ix, iy + 1, iz) == 0 ) {
						faces |= TOP_FACE;
						showing++ ;
					}
					if ( iz > 0 && getBlockType(ix, iy, iz - 1) == 0 ) {
						faces |= FRONT_FACE;
						showing++ ;
					}
					if ( iz + 1 < z_size && getBlockType(ix, iy, iz + 1) == 0 ) {
						faces |= BACK_FACE;
						showing++ ;
					}
					// set the bits used by faces to zero
					data[ix][iy][iz] &= ~( 0xFF << 16 );
					// set the bits to the new value
					data[ix][iy][iz] |= ( faces << 16 );
				}
			}
		}

		System.out.println("Visible Faces: " + showing + " Hidden: "
		        + ( ( solid * 6 ) - showing ));
	}

	private final int getBlockType( int ix, int iy, int iz ) {
		return data[ix][iy][iz] >> 29; // 3 bits;
	}
}
