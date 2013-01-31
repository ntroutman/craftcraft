using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace CraftCraft.Engine
{
    class Properties
    {
        public static bool doOcclusionQueries = false;
        public static bool doBackFaceCulling = true;
        public static bool wireframe = false;

        public static int numQuadsCulled = 0;
        public static int numFacesBackCulled = 0;
        public static int numQuadsRendered = 0;
        public static int numBlocksFrustrumCulled = 0;

        public static void resetRenderAndCullingCounters()
        {
            numBlocksFrustrumCulled = 0;
            numQuadsCulled = 0;
            numQuadsRendered = 0;
            numFacesBackCulled = 0;
        }

        public static String getRenderAndCullingString()
        {

            return String.Format("BFrusCul={0}, BFBackFaceCul={1}, QR={2}",
                    numBlocksFrustrumCulled, numFacesBackCulled, numQuadsRendered);
        }
    }
}
