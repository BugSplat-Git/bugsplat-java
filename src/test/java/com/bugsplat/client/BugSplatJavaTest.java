
import java.io.*;
import com.bugsplat.client.util.BugSplatReport;

public class BugSplatJavaTest
{
    public static void main(String[] args)
    {
		test1();
		test2();

		// terminate
		// System.exit(1);
    }

	// soap test
    public static void test1()
    {
		// TODO BG fix
		// try {
		// 	boolean validated = BugSplatReport.AbleToSend("Fred", "MyJavaCrasher", "1.0");
		// 	if (validated)
		// 		System.out.println("AbleToSend: passed");
		// 	else
		// 		System.out.println("AbleToSend: failed");
		// }
		// catch (Exception e) {
		// 	System.out.println("AbleToSend exception: " + e.toString());
		// }
		// catch (Throwable t) {
		// 	System.out.println("AbleToSend throwable: " + t.toString());
		// }
	}

	// form post, SSL test
	public static void test2()
	{
		// TODO BG
		// try {
		// 	boolean posted = BugSplatReport.PretendPostDumpFile("test.zip", "Fred", "MyJavaCrasher", "1.0", "Java stack trace");
		// 	if (posted)
		// 		System.out.println("PretendPostDumpFile: passed");
		// 	else
		// 		System.out.println("PretendPostDumpFile: failed");
		// }
		// catch (Exception e) {
		// 	System.out.println("PretendPostDumpFile exception: " + e.toString());
		// }
		// catch (Throwable t) {
		// 	System.out.println("PretendPostDumpFile throwable: " + t.toString());
		// }
	}

}