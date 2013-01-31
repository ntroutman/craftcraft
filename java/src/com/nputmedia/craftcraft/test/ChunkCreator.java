package com.nputmedia.craftcraft.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.nputmedia.craftcraft.graphics.primitives.Point3f;
import com.nputmedia.craftcraft.world.Chunk;

/**
 * Creates a simple chunk for testing.
 * 
 * @author Nathaniel Troutman
 * 
 */
public class ChunkCreator {
	private static final int SOLID_GRASS = ( Chunk.BLOCK_TYPE_SOLID << 29 )
	        | ( 0xFF << 16 ) | ( Chunk.BLOCK_TEXTURE_GRASS << 8 ) | 0xFF;
	private static final int SOLID_STONE = ( Chunk.BLOCK_TYPE_SOLID << 29 )
	        | ( 0xFF << 16 ) | ( Chunk.BLOCK_TEXTURE_STONE << 8 ) | 0xFF;
	private static final int SOLID_SAND = ( Chunk.BLOCK_TYPE_SOLID << 29 )
	        | ( 0xFF << 16 ) | ( Chunk.BLOCK_TEXTURE_SAND << 8 ) | 0xFF;

	public static void main( String[] args ) {
		chunkFromHeightMap("/z/heightmap.chunk", "/z/heightmap.gif", 1);
		chunkFromHeightMap("/z/heightmap_medium.chunk", "/z/heightmap.gif", 2);
		chunkFromHeightMap("/z/heightmap_small.chunk", "/z/heightmap.gif", 4);
		faceCheckChunk();
		randomChunk("/z/random.chunk", 32, 32, 32);
		// randomChunk("/z/random_large.chunk", 64, 32, 64);
		// randomChunk("/z/random_huge.chunk", 128, 128, 128);
		System.out.println("Done!");
	}

	public static void faceCheckChunk() {
		Chunk chunk = new Chunk();
		int x_size = 8;
		int y_size = 8;
		int z_size = 8;

		int[][][] data = new int[x_size][y_size][z_size];

		data[1][1][1] = SOLID_GRASS;
		data[1][2][1] = SOLID_GRASS;
		data[2][1][1] = SOLID_GRASS;
		data[3][1][1] = SOLID_GRASS;

		chunk.setData(Point3f.ZERO, data);
		String filename = "/z/faceCheck.chunk";
		chunk.save(filename);
	}

	public static void randomChunk( String filename, int x_size, int y_size,
	        int z_size ) {
		Chunk chunk = new Chunk();

		int[][][] data = new int[x_size][y_size][z_size];

		int block = 0;
		for ( int ix = 0; ix < x_size; ix++ ) {
			for ( int iy = 0; iy < y_size; iy++ ) {
				for ( int iz = 0; iz < z_size; iz++ ) {
					if ( ( ix == 0 || iy == 0 || iz == 0 )
					        || ( ix == ( x_size - 1 ) || iy == ( y_size - 1 ) || iz == ( z_size - 1 ) ) ) {
						block = SOLID_GRASS;

					} else {
						if ( Math.random() < .2 ) {
							int texture = (int) ( ( Math.random() * 3 ) + 1 );
							block = ( Chunk.BLOCK_TYPE_SOLID << 29 )
							        | ( 0x00 << 16 ) | ( texture << 8 ) | 0xFF;
							// block = SOLID_GRASS;
						} else {
							block = Chunk.BLOCK_TYPE_EMPTY;
						}
					}

					data[ix][iy][iz] = block;
				}
			}
		}
		chunk.setData(Point3f.ZERO, data);
		chunk.save(filename);
	}

	public static void chunkFromHeightMap( String filename,
	        String heightMapFilename, int scale ) {
		Chunk chunk = new Chunk();

		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(heightMapFilename));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int x_size = image.getWidth() / scale;
		int z_size = image.getHeight() / scale;
		int y_size = 32;

		int[][][] data = new int[x_size][y_size][z_size];

		for ( int x = 0; x < x_size; x++ ) {
			for ( int z = 0; z < z_size; z++ ) {
				int pixel = image.getRGB(x, z); // TYPE_INT_ARGB
				// byte b4 = (byte) ( pixel >> 24 & 0xFF );
				// byte b3 = (byte) ( pixel >> 16 & 0xFF );
				// byte b2 = (byte) ( pixel >> 8 & 0xFF );
				int b1 = ( pixel & 0xFF );
				int y = (int) ( ( b1 / 256. ) * y_size );
				data[x][y][z] = SOLID_GRASS;
				y -= 1;
				for ( ; y >= 0; y-- ) {
					data[x][y][z] = SOLID_STONE;
				}
			}
		}

		chunk.setData(Point3f.ZERO, data);
		chunk.save(filename);
	}
}
