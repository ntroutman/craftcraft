using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;

namespace CraftCraft.Engine.Primitives
{
    class Box
    {
        public Vector3 corner;
        public Vector3 size;

        public Box()
        {
            corner = new Vector3();
            size = new Vector3();
        }

        public bool containsPoint(Vector3 point)
        {
            return Box.containsPoint(corner, size, point);
        }

        public static bool containsPoint(Vector3 corner, Vector3 size,
                Vector3 point)
        {
            return (corner.X < point.X && point.X < corner.X + size.X)
                    && (corner.Y < point.Y && point.Y < corner.Y + size.Y)
                    && (corner.Z < point.Z && point.Z < corner.Z + size.Z);
        }

        public static void getVertexInfo(FaceBuffers faceBuffers,
                int faces, Vector3 corner)
        {
            getVertexInfo(faceBuffers, faces, (int)corner.X,
                    (int)corner.Y, (int)corner.Y, 1, 1, 1);
        }

        public static void getVertexInfo(FaceBuffers faceBuffers,
                int faces, Vector3 corner, Vector3 size)
        {
            getVertexInfo(faceBuffers, faces, (int)corner.X,
                    (int)corner.Y, (int)corner.Y, (int)size.X, (int)size.Y,
                    (int)size.Z);
        }

        public static void getVertexInfo(FaceBuffers faceBuffers,
                int faces, int x, int y, int z)
        {
            getVertexInfo(faceBuffers, faces, x, y, z, 1, 1, 1);
        }

        public static void getVertexInfo(FaceBuffers faceBuffers,
                int faces, int x, int y, int z, int x_size, int y_size, int z_size)
        {

            List<VertexInfo> verts;
            
            // Front Face
            if ((faces & FaceBuffers.FRONT_FACE_MASK) != 0)
            {
                verts = faceBuffers.rawVertexMap[FaceBuffers.FRONT_FACE];
                verts.Add(new VertexInfo(x, y, z, 1.0f, 0.0f));
                verts.Add(new VertexInfo(x, y + 1, z, 1.0f, 1.0f));
                verts.Add(new VertexInfo(x+1, y + 1, z, 0.0f, 1.0f));
                verts.Add(new VertexInfo(x + 1, y, z, 0.0f, 0.0f));

            }

            // Back Face
            if ((faces & FaceBuffers.BACK_FACE_MASK) != 0)
            {
                verts = faceBuffers.rawVertexMap[FaceBuffers.BACK_FACE];
                verts.Add(new VertexInfo(x, y, z + 1, 0.0f, 0.0f));
                verts.Add(new VertexInfo(x + 1, y, z + 1, 1.0f, 0.0f));
                verts.Add(new VertexInfo(x + 1, y + 1, z + 1, 1.0f, 1.0f));
                verts.Add(new VertexInfo(x, y + 1, z + 1, 0.0f, 1.0f));
            }

            // Top Face
            if ((faces & FaceBuffers.TOP_FACE_MASK) != 0)
            {
                verts = faceBuffers.rawVertexMap[FaceBuffers.TOP_FACE];
                verts.Add(new VertexInfo(x, y+1, z, 0.0f, 1.0f));
                verts.Add(new VertexInfo(x, y+1, z+1, 0.0f, 0.0f));
                verts.Add(new VertexInfo(x+1, y+1, z+1, 1.0f, 0.0f));
                verts.Add(new VertexInfo(x+1, y+1, z, 1.0f, 1.0f));
            }

            // Bottom Face
            if ((faces & FaceBuffers.BOTTOM_FACE_MASK) != 0)
            {
                verts = faceBuffers.rawVertexMap[FaceBuffers.BOTTOM_FACE];
                verts.Add(new VertexInfo(x, y, z, 1.0f, 1.0f));
                verts.Add(new VertexInfo(x+1, y, z, 0.0f, 1.0f));
                verts.Add(new VertexInfo(x+1, y, z+1, 0.0f, 0.0f));
                verts.Add(new VertexInfo(x, y, z+1, 1.0f, 0.0f));
            }

            // Right face
            if ((faces & FaceBuffers.RIGHT_FACE_MASK) != 0)
            {
                verts = faceBuffers.rawVertexMap[FaceBuffers.RIGHT_FACE];
                verts.Add(new VertexInfo(x+1, y, z, 1.0f, 0.0f));
                verts.Add(new VertexInfo(x + 1, y+1, z, 1.0f, 1.0f));
                verts.Add(new VertexInfo(x + 1, y+1, z + 0, 0.0f, 1.0f));
                verts.Add(new VertexInfo(x+1, y, z + 1, 0.0f, 0.0f));
            }

            // Left Face
            if ((faces & FaceBuffers.LEFT_FACE_MASK) != 0)
            {
                verts = faceBuffers.rawVertexMap[FaceBuffers.LEFT_FACE];
                verts.Add(new VertexInfo(x, y, z, 0.0f, 0.0f));
                verts.Add(new VertexInfo(x, y, z+1, 1.0f, 0.0f));
                verts.Add(new VertexInfo(x, y+1, z + 1, 1.0f, 1.0f));
                verts.Add(new VertexInfo(x, y+1, z, 0.0f, 1.0f));
            }
        }

    }
}
