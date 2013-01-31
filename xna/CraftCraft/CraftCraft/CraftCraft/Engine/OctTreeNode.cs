using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;
using CraftCraft.Engine.Primitives;
using Microsoft.Xna.Framework.Graphics;

namespace CraftCraft.Engine
{
    class OctTreeNode
    {
        private static float CULL_ANGLE = (float)Math
            .Cos(MathHelper.ToRadians(45));
        private Chunk chunk;
        private Vector3 corner;
        private Vector3 size;
        private OctTreeNode[] children = null;
        private FaceBuffers boundingBoxBuffers;
        private BoundingBox bbox;
        private BoundingSphere bsphere;

        private Dictionary<int, FaceBuffers> blocktypeFaceBuffers = new Dictionary<int, FaceBuffers>();
        private bool buffersMade = false;

        public OctTreeNode(Chunk chunk, Vector3 corner, Vector3 size)
        {
            this.chunk = chunk;
            this.corner = corner;
            this.size = size;
            bbox = new BoundingBox(corner, corner + size);
            bsphere = new BoundingSphere(corner + (size / 2), size.Length() / 2);

            // While the chunk is to big subdivide things
            if (size.X > 4 || size.Y > 4 || size.Z > 4)
            {
                children = new OctTreeNode[8];
                Vector3 new_size = size * .5f;
                Vector3[] corners = { corner, Vector3.Add(corner, new Vector3(new_size.X, 0, 0)),
			        Vector3.Add(corner, new Vector3(new_size.X, new_size.Y, 0)),
			        Vector3.Add(corner, new Vector3(new_size.X, 0, new_size.Y)),
			        Vector3.Add(corner, new_size), 
                    Vector3.Add(corner, new Vector3(0, new_size.Y, 0)),
			        Vector3.Add(corner, new Vector3(0, new_size.Y, new_size.Z)),
			        Vector3.Add(corner, new Vector3(0, 0, new_size.Z)) };

                for (int i = 0; i < corners.Length; i++)
                {
                    children[i] = new OctTreeNode(chunk, corners[i], new_size);
                }
            }
            else
            {
                boundingBoxBuffers = new FaceBuffers();
                Box.getVertexInfo(boundingBoxBuffers, 0xFF, corner, size);
            }
        }

        public void buildRenderList(Camera camera, List<OctTreeNode> renderList)
        {
            _buildRenderList(camera, renderList);
        }

        private void _buildRenderList(Camera camera, List<OctTreeNode> renderList)
        {
            // test the bounding sphere first
            //FRUSTRUM_CHECK check = camera.frustrumSphereCheck(center, radius);
            ContainmentType check = camera.frustrum.Contains(bsphere);
            switch (check)
            {
                case ContainmentType.Disjoint:
                    Properties.numBlocksFrustrumCulled += chunk.countNonEmpty(corner,
                            size);
                    return;
                case ContainmentType.Contains:
                    break;
                case ContainmentType.Intersects:
                    // check if the bound box is in view
                    check = camera.frustrum.Contains(bbox);
                    switch (check)
                    {
                        case ContainmentType.Contains:
                            break;
                        case ContainmentType.Disjoint:
                            Properties.numBlocksFrustrumCulled += chunk.countNonEmpty(
                                    corner, size);
                            return;
                    }
                    break;
            }
            if (children != null)
            {
                foreach (OctTreeNode child in children)
                {
                    child._buildRenderList(camera, renderList);
                }
                return;
            }

            if (!buffersMade)
            {
                makeBuffers();
            }

            if (true)
            {
                // We want the blocks sorted by increasing distance
                int index = renderList.BinarySearch(this, new OctTreeNodeComparator(camera));
                if (index < 0)
                {
                    index = ~index;
                }
                renderList.Insert(index, this);
            }
            else
            {
                renderList.Add(this);
            }
        }

        public class OctTreeNodeComparator : IComparer<OctTreeNode>
        {
            private Camera camera;

            public OctTreeNodeComparator(Camera camera)
            {
                this.camera = camera;
            }

            public int Compare(OctTreeNode o1, OctTreeNode o2)
            {
                float d1 = (o1.corner - camera.position).LengthSquared();
                float d2 = (o2.corner - camera.position).LengthSquared();
                if (d1 < d2)
                {
                    return -1;
                }
                else if (d2 > d1)
                {
                    return 1;
                }
                return 0;
            }
        }

        public static void renderNodes(BasicEffect effect, Camera camera,
                List<OctTreeNode> renderList)
        {

            DynamicVertexBuffer vertBuffer;
            for (int texIdx = 1; texIdx < Chunk.textures.Length; texIdx++)
            {
                //Chunk.textures[texIdx].bind();
                effect.Texture = Chunk.textures[texIdx];
                foreach (OctTreeNode node in renderList)
                {
                    if (!node.blocktypeFaceBuffers.ContainsKey(texIdx)) { continue; }

                    FaceBuffers faceBuffers = node.blocktypeFaceBuffers[texIdx];

                    for (int faceDirIdx = 0; faceDirIdx < FaceBuffers.FACE_NORMALS.Length; faceDirIdx++)
                    {

                        // do occlusion queries
                        if (Properties.doOcclusionQueries)
                        {
                            //int queryId = GL15.glGenQueries();
                            //GL15.glBeginQuery(GL15.GL_SAMPLES_PASSED, queryId);
                            //GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                            //GL11.glVertexPointer(3, 0,
                            //        node.boundingBoxBuffers.vertexMap
                            //                .get(faceDirIdx));
                            //GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                            //GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);
                            //GL15.glEndQuery(GL15.GL_SAMPLES_PASSED);
                            //GL15.glDeleteQueries(queryId);
                            //int pixels = GL15.glGetQuery(GL15.GL_SAMPLES_PASSED,
                            //        queryId);
                            //if ( pixels <= 0 ) {
                            //    continue;
                            //}
                        }

                        // check the normal of each face against the camera and only draw forward facing faces
                        if (Properties.doBackFaceCulling
                                && Vector3.Dot(camera.lockedTarget
                                        , FaceBuffers.FACE_NORMALS[faceDirIdx]) < -CULL_ANGLE)
                        {
                            Properties.numFacesBackCulled += 1;
                            continue;
                        }

                        vertBuffer = faceBuffers.vertexMap[faceDirIdx];

                        // if the buffer is null then there are no faces textured with
                        // the current texture
                        if (vertBuffer == null)
                        {                            
                            continue;
                        }

                        effect.GraphicsDevice.SetVertexBuffer(vertBuffer);

                        // we could do (vertBuffer/3) but a division by 2 should
                        // be ever so marginally faster
                        int numVerts = vertBuffer.VertexCount;

                        Properties.numQuadsRendered += numVerts / 4;
                        if (Properties.wireframe)
                        {
                            //GL11.glLineWidth(1);
                            //GL11.glDrawArrays(GL11.GL_LINES, 0, numVerts);
                        }
                        else
                        {
                            effect.GraphicsDevice.DrawPrimitives(PrimitiveType.LineStrip, 0, numVerts / 2);
                        }
                    }
                }
            }
        }

        private void makeBuffers()
        {
            int d;

            int max_x = (int)(corner.X + size.X);
            int max_y = (int)(corner.Y + size.Y);
            int max_z = (int)(corner.Z + size.Z);

            // Render each block of the sub-region
            int[, ,] data = chunk.data;
            for (int x = (int)corner.X; x < max_x; x++)
            {
                for (int y = (int)corner.Y; y < max_y; y++)
                {
                    for (int z = (int)corner.Z; z < max_z; z++)
                    {
                        d = data[x, y, z];

                        BlockShape block_shape = Chunk.BLOCK_SHAPE(d); // 3 bits
                        int faces = Chunk.BLOCK_FACES(d);
                        int block_type = Chunk.BLOCK_TYPE(d);

                        if (block_shape == BlockShape.EMPTY)
                        {
                            continue;
                        }


                        FaceBuffers faceBuffers;
                        if (!blocktypeFaceBuffers.ContainsKey(block_type))
                        {
                            faceBuffers = new FaceBuffers();
                            blocktypeFaceBuffers.Add(block_type, faceBuffers);
                        }
                        faceBuffers = blocktypeFaceBuffers[block_type];

                        // only render blocks which have visible faces
                        if (faces != 0)
                        {
                            switch (block_shape)
                            {
                                case BlockShape.SOLID:
                                    Box.getVertexInfo(faceBuffers, faces, x, y, z);
                                    break;
                                case BlockShape.SLOPE:

                                    break;
                                case BlockShape.JOINT:

                                    break;
                            }
                        }
                    }
                }
            }

            // System.out.println("Chunk: " + corner + "\t# textures="
            // + textureFaceBuffers.size());
            foreach (FaceBuffers faceBuffers in blocktypeFaceBuffers.Values)
            {
                faceBuffers.convertToBuffers();
            }

            buffersMade = true;
        }

    }
}
