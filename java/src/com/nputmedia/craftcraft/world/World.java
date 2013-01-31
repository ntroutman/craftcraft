package com.nputmedia.craftcraft.world;

import java.io.DataInputStream;
import java.util.HashMap;

public class World {
	private final String name;
	HashMap<int[], OldChunk> chunkCache = new HashMap<int[], OldChunk>();

	public World(String worldName) {
		name = worldName;
	}

	private void loadChunk(int x, int y, int z) {
		int[] chunkID = { x, y, z };

		if (chunkCache.containsKey(chunkID))
			return;

		String chunkName = String.format("%d_%d_%d.chunk", x, y, z);
		DataInputStream chunkStream = new DataInputStream(
				World.class.getResourceAsStream(chunkName));
		OldChunk chunk = new OldChunk();
		chunk.load(chunkStream);
		chunkCache.put(chunkID, chunk);
	}

	public void setCurrentChunk(int x, int y, int z) {

	}

}
