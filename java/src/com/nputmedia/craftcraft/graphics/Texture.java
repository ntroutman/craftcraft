package com.nputmedia.craftcraft.graphics;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

public class Texture {
	private final int textureID;
	private final int width;
	private final int height;

	public Texture( String name ) {
		this(name, false);
	}

	public Texture( String name, boolean loadAlpha ) {
		URL url = Texture.class.getResource(name);
		System.out.println("Name: " + name + " URL: " + url);
		BufferedImage image = null;
		try {
			image = ImageIO.read(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		width = image.getWidth();
		height = image.getHeight();

		int numBytes = ( loadAlpha ? 4 : 3 );
		int colorType = ( loadAlpha ? GL11.GL_RGBA : GL11.GL_RGB );

		// Get the data from the buffered image and copy into a direct byte
		// buffer (TYPE_INT_ARGB)
		int[] data = image.getRGB(0, 0, width, height, null, 0, width);
		ByteBuffer buffer = ByteBuffer
		        .allocateDirect(width * height * numBytes);
		for ( int i = 0; i < data.length; i++ ) {
			byte b4 = (byte) ( data[i] >> 24 & 0xFF );
			byte b3 = (byte) ( data[i] >> 16 & 0xFF );
			byte b2 = (byte) ( data[i] >> 8 & 0xFF );
			byte b1 = (byte) ( data[i] & 0xFF );

			if ( loadAlpha ) {
				buffer.put(b3);
				buffer.put(b2);
				buffer.put(b1);
				buffer.put(b4);

			} else {
				buffer.put(b3);
				buffer.put(b2);
				buffer.put(b1);
			}
		}
		buffer.rewind();

		textureID = GL11.glGenTextures();
		System.out.println("TexID: " + textureID);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, numBytes, width, height, 0,
		        colorType, GL11.GL_UNSIGNED_BYTE, buffer);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
		        GL11.GL_NEAREST); // Linear Filtering
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
		        GL11.GL_NEAREST); // Linear Filtering
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
		        GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
		        GL11.GL_REPEAT);
	}

	public void bind() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID); // Select Our Texture
	}
}
