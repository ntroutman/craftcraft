package com.nputmedia.craftcraft;

public class Properties {
	public static boolean wireframe = false;
	public static boolean frustrumCulling = true;
	public static boolean physics = false;

	public static int numQuadsRendered = 0;
	public static int numQuadsCulled = 0;
	public static int numBlocksFrustrumCulled = 0;
	public static int numFacesBackCulled = 0;
	public static boolean doBackFaceCulling = true;
	public static boolean doFrustrumCulling = true;
	public static boolean doOcclusionQueries = false;

	public static void resetRenderAndCullingCounters() {
		numBlocksFrustrumCulled = 0;
		numQuadsCulled = 0;
		numQuadsRendered = 0;
		numFacesBackCulled = 0;
	}

	public static String getRenderAndCullingString() {

		return String.format("BFrusCul=%d, BFBackFaceCul=%s, QR=%s",
		        numBlocksFrustrumCulled, numFacesBackCulled, numQuadsRendered);
	}
}
