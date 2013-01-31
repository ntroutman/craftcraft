package com.nputmedia.craftcraft.world;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.nputmedia.craftcraft.graphics.Camera;
import com.nputmedia.craftcraft.graphics.FaceBuffers;
import com.nputmedia.craftcraft.graphics.OctTreeNode;
import com.nputmedia.craftcraft.graphics.Texture;
import com.nputmedia.craftcraft.graphics.primitives.Box;
import com.nputmedia.craftcraft.graphics.primitives.Point3f;
import com.nputmedia.craftcraft.graphics.primitives.Sphere;

public class Chunk {
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

	public static final int BLOCK_TEXTURE_GRASS = 1;
	public static final int BLOCK_TEXTURE_STONE = 2;
	public static final int BLOCK_TEXTURE_SAND = 3;

	private int x_size;
	private int y_size;
	private int z_size;
	public int[][][] data;
	private OctTreeNode octTree = null;

	private Box bbox = null;
	private Sphere bsphere = null;
	private Point3f corner;
	public static Texture[] textures;

	public Chunk() {
		bbox = new Box();
		bsphere = new Sphere();
	}

	public static void loadTextures() {
		textures = new Texture[4];
		textures[BLOCK_TEXTURE_GRASS] = new Texture("/images/grass.png");
		textures[BLOCK_TEXTURE_STONE] = new Texture("/images/stone1.png");
		textures[BLOCK_TEXTURE_SAND] = new Texture("/images/sand.png");
	}

	public void setData( Point3f newCorner, int[][][] newData ) {
		corner = newCorner;
		x_size = newData.length;
		y_size = newData[0].length;
		z_size = newData[0][0].length;
		data = newData;
		updateBounds();
		fixFaceVisibility();
		octTree = new OctTreeNode(this, corner, new Point3f(x_size, y_size,
		        z_size));
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
			Point3f corner = new Point3f(chunkStream.readInt(),
			        chunkStream.readInt(), chunkStream.readInt());
			x_size = chunkStream.readInt();
			y_size = chunkStream.readInt();
			z_size = chunkStream.readInt();

			System.out.println(String.format("loading chunk: x=%d, y=%d, z=%d",
			        x_size, y_size, z_size));

			int block_type, d;
			int solid = 0, empty = 0;
			data = new int[x_size][y_size][z_size];
			for ( int ix = 0; ix < x_size; ix++ ) {
				for ( int iy = 0; iy < y_size; iy++ ) {
					for ( int iz = 0; iz < z_size; iz++ ) {
						d = chunkStream.readInt();
						data[ix][iy][iz] = d;

						block_type = d >> 29; // 3 bits

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
			setData(corner, data);

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
			chunkStream.writeInt((int) corner.x);
			chunkStream.writeInt((int) corner.y);
			chunkStream.writeInt((int) corner.z);

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

	public void renderChunk( Camera camera, ArrayList<OctTreeNode> renderList ) {
		octTree.render(camera, renderList);
	}

	public int countNonEmpty( Point3f corner, Point3f size ) {
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
	 * Checks each block and determines which faces are visible. By checking the neighboring blocks
	 * to see if they are empty or not.
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
					if ( ix > 0
					        && getBlockType(ix - 1, iy, iz) == BLOCK_TYPE_EMPTY ) {
						faces |= FaceBuffers.LEFT_FACE_MASK;
						showing++ ;
					}
					if ( ix + 1 < x_size
					        && getBlockType(ix + 1, iy, iz) == BLOCK_TYPE_EMPTY ) {
						faces |= FaceBuffers.RIGHT_FACE_MASK;
						showing++ ;
					}
					if ( iy > 0
					        && getBlockType(ix, iy - 1, iz) == BLOCK_TYPE_EMPTY ) {
						faces |= FaceBuffers.BOTTOM_FACE_MASK;
						showing++ ;
					}
					if ( iy + 1 < y_size
					        && getBlockType(ix, iy + 1, iz) == BLOCK_TYPE_EMPTY ) {
						faces |= FaceBuffers.TOP_FACE_MASK;
						showing++ ;
					}
					if ( iz > 0
					        && getBlockType(ix, iy, iz - 1) == BLOCK_TYPE_EMPTY ) {
						faces |= FaceBuffers.FRONT_FACE_MASK;
						showing++ ;
					}
					if ( iz + 1 < z_size
					        && getBlockType(ix, iy, iz + 1) == BLOCK_TYPE_EMPTY ) {
						faces |= FaceBuffers.BACK_FACE_MASK;
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
