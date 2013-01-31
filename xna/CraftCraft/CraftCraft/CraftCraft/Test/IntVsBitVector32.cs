using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections.Specialized;

namespace CraftCraft.Test
{
    class IntVsBitVector32
    {
        internal static void Run(string[] args)
        {
            const int NUM_TESTS = 10000;
            const int NUM_ELEMENTS = 2048;

            BitVector32.Section section1, section2, section3, section4, section5;
            section1 = BitVector32.CreateSection(7);
            section2 = BitVector32.CreateSection(31, section1);
            section3 = BitVector32.CreateSection(255, section2);
            section4 = BitVector32.CreateSection(255, section3);
            section5 = BitVector32.CreateSection(255, section4);

            byte[,] bytes = new byte[NUM_ELEMENTS, 5];
            int[] ints = new int[NUM_ELEMENTS];
            BitVector32[] bvs = new BitVector32[NUM_ELEMENTS];

            Random gen = new Random(112358);
            for (int i = 0; i < NUM_ELEMENTS; i++)
            {
                int b1 = gen.Next(8), b2 = gen.Next(16), b3 = gen.Next(256), b4 = gen.Next(256), b5 = gen.Next(256);
                int packed = b1 << 29 | b2 << 24 | b3 << 16 | b4 << 8 | b5;
                ints[i] = packed;
                bytes[i, 0] = (byte)b1;
                bytes[i, 1] = (byte)b2;
                bytes[i, 2] = (byte)b3;
                bytes[i, 3] = (byte)b4;
                bytes[i, 4] = (byte)b5;
                bvs[i] = new BitVector32(packed);
            }

            Timer.timeit("Read Ints", NUM_TESTS, delegate()
            {
                for (int i = 0; i < NUM_ELEMENTS; i++)
                {
                    int d = ints[i];
                    int b1 = (d >> 29); // 3 bits
                    int b2 = (d >> 24) & 0x1f; // 5 bits
                    int b3 = (d >> 16) & 0xFF; // 8 bits
                    int b4 = (d >> 8) & 0xFF; // 8 bits
                    int b5 = d & 0xFF; // 8 bits
                    int t = b1 + b2 + b3 + b4 + b5;
                }
            });


            Timer.timeit("Read BitVector32", NUM_TESTS, delegate()
            {
                for (int i = 0; i < NUM_ELEMENTS; i++)
                {
                    BitVector32 bv = bvs[i];
                    int b1 = bv[section1];
                    int b2 = bv[section2];
                    int b3 = bv[section3];
                    int b4 = bv[section4];
                    int b5 = bv[section5];
                    int t = b1 + b2 + b3 + b4 + b5;
                }
            });

            Timer.timeit("Read Bytes", NUM_TESTS, delegate()
            {
                for (int i = 0; i < NUM_ELEMENTS; i++)
                {
                    int b1 = bytes[i, 0];
                    int b2 = bytes[i, 1];
                    int b3 = bytes[i, 2];
                    int b4 = bytes[i, 3];
                    int b5 = bytes[i, 4];
                    int t = b1 + b2 + b3 + b4 + b5;
                }
            });
        }
    }
}
