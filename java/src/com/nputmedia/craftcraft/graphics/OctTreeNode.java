package com.nputmedia.craftcraft.graphics;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import com.nputmedia.craftcraft.Properties;
import com.nputmedia.craftcraft.graphics.Camera.FRUSTRUM_CHECK;
import com.nputmedia.craftcraft.graphics.primitives.Box;
import com.nputmedia.craftcraft.graphics.primitives.Point3f;
import com.nputmedia.craftcraft.world.Chunk;

public class OctTreeNode {

	private static final float CULL_ANGLE = (float) Math
	        .cos(Math.toRadians(45));
	private final Chunk chunk;
	private final Point3f corner;
	private final Point3f size;
	private OctTreeNode[] children = null;
	private FaceBuffers boundingBoxBuffers;

	private final HashMap<Integer, FaceBuffers> textureFaceBuffers = new HashMap<Integer, FaceBuffers>();
	private boolean buffersMade = false;

	public OctTreeNode( Chunk chunk, Point3f corner, Point3f size ) {
		this.chunk = chunk;
		this.corner = corner;
		this.size = size;

		// While the chunk is to big subdivide things
		if ( size.x > 4 || size.y > 4 || size.z > 4 ) {
			children = new OctTreeNode[8];
			Point3f new_size = size.scale(.5f);
			Point3f[] corners = { corner, corner.add(new_size.x, 0, 0),
			        corner.add(new_size.x, new_size.y, 0),
			        corner.add(new_size.x, 0, new_size.y),
			        corner.add(new_size), corner.add(0, new_size.y, 0),
			        corner.add(0, new_size.y, new_size.z),
			        corner.add(0, 0, new_size.z) };

			for ( int i = 0; i < corners.length; i++ ) {
				children[i] = new OctTreeNode(chunk, corners[i], new_size);
			}
		} else {
			boundingBoxBuffers = new FaceBuffers();
			Box.getBoxVertsAndTexCoords(boundingBoxBuffers, 0xFF, corner, size);
		}
	}

	public void render( Camera camera, ArrayList<OctTreeNode> renderList ) {
		_render(camera, renderList);
	}

	private void _render( Camera camera, ArrayList<OctTreeNode> renderList ) {
		Point3f center = size.scale(.5f).addLocal(corner);
		float radius = size.magnitude() * .5f;
		// test the bounding sphere first
		FRUSTRUM_CHECK check = camera.frustrumSphereCheck(center, radius);
		switch ( check ) {
		case OUTSIDE:
			Properties.numBlocksFrustrumCulled += chunk.countNonEmpty(corner,
			        size);
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
				Properties.numBlocksFrustrumCulled += chunk.countNonEmpty(
				        corner, size);
				return;
			}
			break;
		}
		if ( children != null ) {
			for ( OctTreeNode child : children ) {
				child._render(camera, renderList);
			}
			return;
		}

		if ( !buffersMade ) {
			makeBuffers();
		}

		if ( true ) {
			// We want the blocks sorted by increasing distance
			int index = Collections.binarySearch(renderList, this,
			        new OctTreeNodeComparator(camera));
			if ( index < 0 ) {
				index = -index - 1;
			}
			renderList.add(index, this);
		} else {
			renderList.add(this);
		}
	}

	public class OctTreeNodeComparator implements Comparator<OctTreeNode> {
		private final Camera camera;

		public OctTreeNodeComparator( Camera camera ) {
			this.camera = camera;
		}

		public int compare( OctTreeNode o1, OctTreeNode o2 ) {
			float d1 = o1.corner.distanceSquaredTo(camera.pos);
			float d2 = o2.corner.distanceSquaredTo(camera.pos);
			if ( d1 < d2 ) {
				return -1;
			} else if ( d2 > d1 ) {
				return 1;
			}
			return 0;
		}
	}

	public static void renderNodes( Camera camera,
	        ArrayList<OctTreeNode> renderList ) {

		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		int DRAW_TYPE = GL11.GL_QUADS;

		FloatBuffer vertBuffer, texBuffer;
		for ( int texIdx = 1; texIdx < Chunk.textures.length; texIdx++ ) {
			Chunk.textures[texIdx].bind();
			for ( OctTreeNode node : renderList ) {
				FaceBuffers faceBuffers = node.textureFaceBuffers.get(texIdx);
				if ( faceBuffers == null ) {
					// no faces for this texture
					continue;
				}

				for ( int faceDirIdx = 0; faceDirIdx < FaceBuffers.FACE_NORMALS.length; faceDirIdx++ ) {

					// do occlusion queries
					if ( Properties.doOcclusionQueries ) {
						int queryId = GL15.glGenQueries();
						GL15.glBeginQuery(GL15.GL_SAMPLES_PASSED, queryId);
						GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
						GL11.glVertexPointer(3, 0,
						        node.boundingBoxBuffers.vertexMap
						                .get(faceDirIdx));
						GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
						GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);
						GL15.glEndQuery(GL15.GL_SAMPLES_PASSED);
						GL15.glDeleteQueries(queryId);
						int pixels = GL15.glGetQuery(GL15.GL_SAMPLES_PASSED,
						        queryId);
						if ( pixels <= 0 ) {
							continue;
						}
					}

					// check the normal of each face against the camera and only draw forward facing
					// faces
					if ( Properties.doBackFaceCulling
					        && camera.lockedN
					                .dot(FaceBuffers.FACE_NORMALS[faceDirIdx]) < -CULL_ANGLE ) {
						Properties.numFacesBackCulled += 1;
						continue;
					}

					vertBuffer = faceBuffers.vertexMap.get(faceDirIdx);

					// if the buffer is null then there are no faces textured with
					// the current texture
					if ( vertBuffer == null ) {
						continue;
					}

					texBuffer = faceBuffers.textureMap.get(faceDirIdx);

					// we could do (vertBuffer/3) but a division by 2 should
					// be ever so marginally faster
					int numVerts = texBuffer.capacity() / 2;

					vertBuffer.flip();
					texBuffer.flip();
					GL11.glVertexPointer(3, 0, vertBuffer);
					GL11.glTexCoordPointer(2, 0, texBuffer);

					Properties.numQuadsRendered += numVerts / 4;
					if ( Properties.wireframe ) {
						GL11.glLineWidth(1);
						GL11.glDrawArrays(GL11.GL_LINES, 0, numVerts);
					} else {
						GL11.glDrawArrays(DRAW_TYPE, 0, numVerts);
					}
				}
			}
		}
		GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
	}

	private void makeBuffers() {
		int block_type, faces, subtexture, texture, rotation, d;
		int max_x = (int) ( corner.x + size.x );
		int max_y = (int) ( corner.y + size.y );
		int max_z = (int) ( corner.z + size.z );

		// Render each block of the sub-region
		int[][][] data = chunk.data;
		for ( int x = (int) corner.x; x < max_x; x++ ) {
			for ( int y = (int) corner.y; y < max_y; y++ ) {
				for ( int z = (int) corner.z; z < max_z; z++ ) {
					d = data[x][y][z];

					block_type = d >> 29; // 3 bits

					if ( block_type == Chunk.BLOCK_TYPE_EMPTY ) {
						continue;
					}

					rotation = d >> 24 & 0x1f; // 5 bits
					faces = d >> 16 & 0xFF; // 8 bits
					texture = d >> 8 & 0xFF; // 8 bits
					subtexture = d & 0xFF; // 8 bits

					FaceBuffers faceBuffers = textureFaceBuffers.get(texture);
					if ( faceBuffers == null ) {
						faceBuffers = new FaceBuffers();
						textureFaceBuffers.put(texture, faceBuffers);
					}

					// only render blocks which have visible faces
					if ( faces != 0 ) {
						switch ( block_type ) {
						case Chunk.BLOCK_TYPE_SOLID:
							Box.getBoxVertsAndTexCoords(faceBuffers, faces, x,
							        y, z);
							break;
						case Chunk.BLOCK_TYPE_SLOPE:

							break;
						case Chunk.BLOCK_TYPE_JOINT:

							break;
						}
					}
				}
			}
		}

		// System.out.println("Chunk: " + corner + "\t# textures="
		// + textureFaceBuffers.size());
		for ( FaceBuffers faceBuffers : textureFaceBuffers.values() ) {
			faceBuffers.convertToBuffers();
		}

		buffersMade = true;
	}

	private void _renderDirect() {
		// The sub-region of the chunk is now small enough for rendering
		int block_type, faces, subtexture, texture, rotation, d;
		int max_x = (int) ( corner.x + size.x );
		int max_y = (int) ( corner.y + size.y );
		int max_z = (int) ( corner.z + size.z );

		// Render each block of the sub-region
		int[][][] data = chunk.data;
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
						Chunk.textures[texture].bind();
						switch ( block_type ) {
						case Chunk.BLOCK_TYPE_SOLID:
							Properties.numQuadsRendered += Box.renderBox(faces,
							        x, y, z);
							break;
						case Chunk.BLOCK_TYPE_SLOPE:

							break;
						case Chunk.BLOCK_TYPE_JOINT:

							break;
						}
					}
				}
			}
		}
	}
}
