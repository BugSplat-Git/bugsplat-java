import java.applet.*;
import java.awt.*;

import com.bugsplat.BugSplat;

public class MyJavaCrasherApplet extends Applet {

   String labels[] = {"Null Pointer Exception",
                      "Divide By Zero",
   					  "Index Out Of Bounds",
                      "Negative Array Size"};

   Button b1 = new Button(labels[0]);
   Button b2 = new Button(labels[1]);
   Button b3 = new Button(labels[2]);
   Button b4 = new Button(labels[3]);

   public void init()
     {
        // init the bugsplat library
        BugSplat.Init("Fred","MyJavaCrasherApplet","1.0");

       setLayout(null);
       add(b1);
       add(b2);
       add(b3);
       add(b4);

       int width = 200;
       int height = 40;
       int hspace = 25;
       int vspace = 20;

       b1.reshape(hspace, vspace + (vspace + height)*0, width, height);
       b2.reshape(hspace, vspace + (vspace + height)*1, width, height);
       b3.reshape(hspace, vspace + (vspace + height)*2, width, height);
       b4.reshape(hspace, vspace + (vspace + height)*3, width, height);
     }

	public boolean action(Event evt, Object arg)
	{
		try
		{
			if (evt.target instanceof Button)
			{
				if (arg.equals(labels[0])) {
					test1();
				}
				else if(arg.equals(labels[1])) {
					test2();
				}
				else if(arg.equals(labels[2])) {
					test3();
				}
				else if(arg.equals(labels[3])) {
					test4();
				}

				return true;        // event processed
			}
		}
		catch (Exception ex)
		{
			BugSplat.HandleException(ex);
		}
		return false;            // event not processed
	}

	public void test1()
	{
		Object o = null;
		String temp = o.toString();
	}
	public void test2()
	{
		int x = 1;
		int y = 0;
		int z = x / y;
	}
	public void test3()
	{
	   String arr[] = {"One", "Two"};
	   String temp = arr[2];
	}
	public void test4()
	{
		int len = -1;
	    String arr[] = new String[len];
	}
}
