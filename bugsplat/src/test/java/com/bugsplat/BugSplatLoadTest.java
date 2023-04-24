package com.bugsplat;

import java.util.Date;

/*
 * Created on Nov 10, 2006
 *
 */

/**
 * @author Harm-Jan Spier
 *
 */
public class BugSplatLoadTest
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            BugSplat.init("Fred", "MyJavaCrasher", "1.0");
            BugSplat.setQuietMode(true);
            for(int i=0;i<1000;i++)
            {
                System.out.println("======================"+i+"====================== "+new Date());
                BugSplat.handleException(new Exception(""+i));
                Thread.sleep(1000);
            }
            System.out.println("End of test");
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        //BugSplat.SetTerminateApplication(true);
        System.exit(1);
    }

}
