using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework.Content;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework;
using CraftCraft.Engine.Primitives;
using System.IO;
using System.Collections.Specialized;

namespace CraftCraft.Engine
{
    public enum BlockType
    {
        GRASS = 1, STONE, SAND
    }

    public enum BlockShape
    {
        EMPTY = 0,
        SOLID,
        SLOPE,
        CORNER,
        JOINT
    }

    class Chunk
    {
        private int x_size;
        private int y_size;
        private int z_size;
        public int[, ,] data;
        private OctTreeNode octTree = null;

        private BoundingBox bbox;
        private BoundingSphere bsphere;
        private Vector3 corner;
        public static Texture2D[] textures;

        public Chunk()
        {
            bbox = new BoundingBox();
            bsphere = new BoundingSphere();
        }

        public static void loadTextures(ContentManager content)
        {
            textures = new Texture2D[4];
            textures[(int)BlockType.GRASS] = content.Load<Texture2D>("Images/grass");
            textures[(int)BlockType.STONE] = content.Load<Texture2D>("Images/stone1");
            textures[(int)BlockType.SAND] = content.Load<Texture2D>("Images/sand");
        }

        public void setData(Vector3 newCorner, int[, ,] newData)
        {
            corner = newCorner;
            x_size = newData.GetLength(0);
            y_size = newData.GetLength(1);
            z_size = newData.GetLength(2);
            data = newData;
            updateBounds();
            fixFaceVisibility();
            octTree = new OctTreeNode(this, corner, new Vector3(x_size, y_size,
                    z_size));
        }

        private void updateBounds()
        {
            bbox.Max = bbox.Min + new Vector3(x_size, y_size, z_size);

            bsphere.Center = bbox.Min + (bbox.Max / 2);
            bsphere.Radius = (bsphere.Center - bbox.Min).Length();
        }

        public void load(String filename)
        {
            Console.WriteLine("Loading: " + filename);

            load(new BinaryReader(new FileStream(filename, FileMode.OpenOrCreate)));
        }

        public void load(BinaryReader chunkStream)
        {
            Vector3 corner = new Vector3(chunkStream.ReadInt32(),
                    chunkStream.ReadInt32(), chunkStream.ReadInt32());
            x_size = chunkStream.ReadInt32();
            y_size = chunkStream.ReadInt32();
            z_size = chunkStream.ReadInt32();

            Console.WriteLine(String.Format("loading chunk: x=%d, y=%d, z=%d",
                    x_size, y_size, z_size));

            int d;
            BlockShape block_shape;
            int solid = 0, empty = 0;
            data = new int[x_size, y_size, z_size];

            for (int ix = 0; ix < x_size; ix++)
            {
                for (int iy = 0; iy < y_size; iy++)
                {
                    for (int iz = 0; iz < z_size; iz++)
                    {
                        d = chunkStream.ReadInt32();
                        data[ix, iy, iz] = d;

                        block_shape = BLOCK_SHAPE(d);

                        if (block_shape != BlockShape.EMPTY)
                        {
                            solid++;
                        }
                        else
                        {
                            empty++;
                        }
                    }
                }
            }
            Console.WriteLine("\tsolid=" + solid);
            Console.WriteLine("\tempty=" + empty);
            Console.WriteLine("\ttotal=" + (solid + empty));
            setData(corner, data);

        }

        public static BlockShape BLOCK_SHAPE(int d)
        {
            return (BlockShape)( d >> 29 );
        }

        public static int BLOCK_FACES(int d)
        {
            return (d >> 24) & 0x1f; // 5 bits
        }

        public static  int BLOCK_ROTATION(int d)
        {
            return (d >> 16) & 0xFF; // 8 bits
        }

        public static int BLOCK_TYPE(int d)
        {
            return (d >> 8) & 0xFF; // 8 bits
        }

        public static  int BLOCK_TEXTURE(int d)
        {
            return d & 0xFF; // 8 bits
        }


        public void save(String filename)
        {
            Console.WriteLine("Saving: " + filename);
            save(new BinaryWriter(new FileStream(filename, FileMode.OpenOrCreate)));
        }

        public void save(BinaryWriter chunkStream)
        {
            chunkStream.Write((int)corner.X);
            chunkStream.Write((int)corner.Y);
            chunkStream.Write((int)corner.Z);

            chunkStream.Write(x_size);
            chunkStream.Write(y_size);
            chunkStream.Write(z_size);

            for (int ix = 0; ix < x_size; ix++)
            {
                for (int iy = 0; iy < y_size; iy++)
                {
                    for (int iz = 0; iz < z_size; iz++)
                    {
                        chunkStream.Write(data[ix, iy, iz]);
                    }
                }
            }
        }

        public void buildRenderList(Camera camera, List<OctTreeNode> renderList)
        {
            octTree.buildRenderList(camera, renderList);
        }

        public int countNonEmpty(Vector3 corner, Vector3 size)
        {
            int d, solid = 0;
            BlockShape block_shape;
            int max_x = (int)(corner.X + size.X);
            int max_y = (int)(corner.Y + size.Y);
            int max_z = (int)(corner.Z + size.Z);
            for (int x = (int)corner.X; x < max_x; x++)
            {
                for (int y = (int)corner.Y; y < max_y; y++)
                {
                    for (int z = (int)corner.Z; z < max_z; z++)
                    {
                        d = data[x, y, z];

                        block_shape = BLOCK_SHAPE(d);

                        if (block_shape != BlockShape.EMPTY)
                        {
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
        private void fixFaceVisibility()
        {
            BlockShape block_type;
            int faces, showing = 0, solid = 0;
            for (int ix = 0; ix < x_size; ix++)
            {
                for (int iy = 0; iy < y_size; iy++)
                {
                    for (int iz = 0; iz < z_size; iz++)
                    {
                        block_type = getBlockType(ix, iy, iz);
                        if (block_type == BlockShape.EMPTY)
                            continue;
                        solid++;
                        faces = 0;
                        if (ix > 0
                                && getBlockType(ix - 1, iy, iz) == BlockShape.EMPTY)
                        {
                            faces |= FaceBuffers.LEFT_FACE_MASK;
                            showing++;
                        }
                        if (ix + 1 < x_size
                                && getBlockType(ix + 1, iy, iz) == BlockShape.EMPTY)
                        {
                            faces |= FaceBuffers.RIGHT_FACE_MASK;
                            showing++;
                        }
                        if (iy > 0
                                && getBlockType(ix, iy - 1, iz) == BlockShape.EMPTY)
                        {
                            faces |= FaceBuffers.BOTTOM_FACE_MASK;
                            showing++;
                        }
                        if (iy + 1 < y_size
                                && getBlockType(ix, iy + 1, iz) == BlockShape.EMPTY)
                        {
                            faces |= FaceBuffers.TOP_FACE_MASK;
                            showing++;
                        }
                        if (iz > 0
                                && getBlockType(ix, iy, iz - 1) == BlockShape.EMPTY)
                        {
                            faces |= FaceBuffers.FRONT_FACE_MASK;
                            showing++;
                        }
                        if (iz + 1 < z_size
                                && getBlockType(ix, iy, iz + 1) == BlockShape.EMPTY)
                        {
                            faces |= FaceBuffers.BACK_FACE_MASK;
                            showing++;
                        }
                        // set the bits used by faces to zero
                        data[ix, iy, iz] &= ~(0xFF << 16);
                        // set the bits to the new value
                        data[ix, iy, iz] |= (faces << 16);
                    }
                }
            }

            Console.WriteLine("Visible Faces: " + showing + " Hidden: "
                    + ((solid * 6) - showing));
        }

        private BlockShape getBlockType(int ix, int iy, int iz)
        {
            return (BlockShape)(data[ix, iy, iz] >> 29); // 3 bits;
        }

    }
}
