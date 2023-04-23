package com.bugsplat;

import org.junit.jupiter.api.Test;

public class BugSplatJavaTest
{
	@Test
    public void HandleException()
    {
		// TODO BG I don't think we can do this because it runs on another thread
//		try {
//			BugSplat.Init("fred", "MyJavaCrasher", "1.0");
//			BugSplat.SetQuietMode(true);
//			DivideByZeroTest();
//		} catch (Exception e) {
//			BugSplat.HandleException(e);
//		}
	}

	//
	// divide by zero test
	//
    private void DivideByZeroTest()
    {
		int x = 1;
		int y = 0;
		int z = x / y;
    }
}