using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace CraftCraft.Engine
{
    class FaceBuffers
    {
        public static Vector3[] FACE_NORMALS = { new Vector3(0, 0, -1),
	        new Vector3(0, 0, 1), new Vector3(0, 1, 0), new Vector3(0, -1, 0),
	        new Vector3(-1, 0, 0), new Vector3(1, 0, 0) };

        public static int FRONT_FACE_MASK = 1;
        public static int BACK_FACE_MASK = 1 << 1;
        public static int TOP_FACE_MASK = 1 << 2;
        public static int BOTTOM_FACE_MASK = 1 << 3;
        public static int LEFT_FACE_MASK = 1 << 4;
        public static int RIGHT_FACE_MASK = 1 << 5;

        public static int FRONT_FACE = 0;
        public static int BACK_FACE = 1;
        public static int TOP_FACE = 2;
        public static int BOTTOM_FACE = 3;
        public static int LEFT_FACE = 4;
        public static int RIGHT_FACE = 5;

        public Dictionary<int, DynamicVertexBuffer> vertexMap;

        public Dictionary<int, List<VertexInfo>> rawVertexMap;


        public static VertexDeclaration VERTEX_DEC = new VertexDeclaration(VertexInfo.VertexElements);

        public FaceBuffers()
        {
            vertexMap = new Dictionary<int, DynamicVertexBuffer>();
            rawVertexMap = new Dictionary<int, List<VertexInfo>>();
            
            for (int faceDirIdx = 0; faceDirIdx < FACE_NORMALS.Length; faceDirIdx++)
            {
                rawVertexMap.Add(faceDirIdx, new List<VertexInfo>());
            }

        }

        public void convertToBuffers()
        {
            for (int faceDirIdx = 0; faceDirIdx < FACE_NORMALS.Length; faceDirIdx++)
            {
                vertexMap.Add(faceDirIdx, bufferFromArrayList(rawVertexMap[faceDirIdx]));

            }
            rawVertexMap.Clear();
        }

        public static DynamicVertexBuffer bufferFromArrayList(List<VertexInfo> verts)
        {
            if (verts.Count == 0) return null;

            DynamicVertexBuffer vertBuffer = new DynamicVertexBuffer(CraftCraftGame.GD, VERTEX_DEC, verts.Count, BufferUsage.WriteOnly);
            vertBuffer.SetData(verts.ToArray<VertexInfo>());

            return vertBuffer;
        }

    }

    public struct VertexInfo
    {
        Vector3 pos;
        Vector2 tex;
        float shade;

        public static readonly VertexElement[] VertexElements = new VertexElement[]
        { 
            new VertexElement(0,VertexElementFormat.Vector3, VertexElementUsage.Position, 0),
            new VertexElement(sizeof(float)*3,VertexElementFormat.Vector2, VertexElementUsage.TextureCoordinate, 0),
            new VertexElement(sizeof(float)*5,VertexElementFormat.Single, VertexElementUsage.TextureCoordinate, 1)               
        };

        public VertexInfo(Vector3 position, Vector2 uv, double shade)
        {
            pos = position;
            tex = uv;
            this.shade = (float)shade;
        }

        public VertexInfo(float x, float y, float z, float s, float t)
        {
            pos = new Vector3(x, y, z);
            tex = new Vector2(s, t);
            shade = 1f;
        }

        public VertexInfo(float x, float y, float z, float s, float t, float shade)
        {
            pos = new Vector3(x, y, z);
            tex = new Vector2(s, t);
            this.shade = shade;
        }

        public Vector3 Position { get { return pos; } set { pos = value; } }
        public Vector2 Tex { get { return tex; } set { tex = value; } }
        public float Shade { get { return shade; } set { shade = value; } }
        public static int SizeInBytes { get { return sizeof(float) * 6; } }
    }


}
