//
// BugSplat integration code for Java applications.
// This class implements an image canvas.
//
// Copyright 2005 BugSplat, LLC.
//
package com.bugsplat.gui;

import java.awt.Canvas;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.MediaTracker;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

public class BugSplatImageCanvas extends Canvas
{
	Image image = null;
	int width = 0;
	int height = 0;

	BugSplatImageCanvas(String filename)
	{
		// use getResourceAsStream so that this works in a JAR

		// TODO: media tracker looks risky - try this
		// Image i = new javax.swing.ImageIcon("tarsier.png").getImage();

		// wait for the image to load
		InputStream is = getClass().getResourceAsStream(filename);
  		BufferedInputStream bis = new BufferedInputStream(is);
  		byte[] byBuf = new byte[4096];  // a buffer large enough for our image
		try
		{
			int byteRead = bis.read(byBuf, 0, 4096);
		}
		catch (IOException ioe) { }

  		image = Toolkit.getDefaultToolkit().createImage(byBuf);

		MediaTracker mediaTracker = new MediaTracker(this);
		mediaTracker.addImage(image, 0);

		try
		{
			mediaTracker.waitForID(0);
		}
		catch (InterruptedException ie)
		{
			System.out.println("InterruptedException in BugSplatImageCanvas: " + ie.toString());
			// System.exit(1);
		}

		width = image.getWidth(this);
	    height = image.getHeight(this);

		setBounds(0, 32, width, height);
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public void paint(Graphics graphics)
	{
		if (image != null)
			graphics.drawImage(image, 0, 0, this);
	}
}
