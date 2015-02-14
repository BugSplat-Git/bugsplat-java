//
// BugSplat integration code for Java applications.
// This class catches exceptions in our exception handler.
//
// Copyright 2005 BugSplat, LLC.
//
package com.bugsplatsoftware.client.util;

public class BugSplatThreadGroup extends ThreadGroup
{
	public BugSplatThreadGroup(String s)
	{
    	super(s);
    }

    public void uncaughtException(Thread thread, Throwable throwable)
    {
    	// TODO: need to handle this gracefully
		System.out.println("BugSplat thread has terminated: " + throwable.toString());
    }
}