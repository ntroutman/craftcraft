using System;
using CraftCraft.Test;

namespace CraftCraft
{
#if WINDOWS || XBOX
    static class Program
    {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        static void Main(string[] args)
        {
            if (false)
            {
                //CraftCraft.Test.IntVsBitVector32.Run(args);
                ChunkCreator.Run(args);
                return;
            }
            using (CraftCraftGame game = new CraftCraftGame())
            {
                game.Run();
            }
        }
    }
#endif
}

