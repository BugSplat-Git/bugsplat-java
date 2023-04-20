//
// BugSplat integration code for Java applications.
// This class implements the thread for the handler.
//
// Copyright 2005 BugSplat, LLC.
//
package com.bugsplat.util;

public class BugSplatThread extends Thread
{
	Runnable r = null;

	public BugSplatThread(ThreadGroup tg, Runnable r)
	{
		super(tg, "BugSplatThread");
		this.r = r;
	}

	public void run()
	{
		if (r != null)
		{
			System.out.println("Starting thread...");
			r.run();
			System.out.println("Thread done.");
		}
	}
}
