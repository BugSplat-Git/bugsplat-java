import java.util.Date;

import com.bugsplatsoftware.client.BugSplat;

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
            BugSplat.Init("Fred", "MyJavaCrasher", "1.0");
            BugSplat.SetQuietMode(true);
            for(int i=0;i<1000;i++)
            {
                System.out.println("======================"+i+"====================== "+new Date());
                BugSplat.HandleException(new Exception(""+i));
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
