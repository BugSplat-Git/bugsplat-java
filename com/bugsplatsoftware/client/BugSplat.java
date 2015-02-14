//
// BugSplat integration code for Java applications.
// This class allows a Java application to post
// BugSplat reports on www.BugSplatSoftware.com
//
// Copyright 2014 BugSplat, LLC.
//
package com.bugsplatsoftware.client;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.text.*;
import javax.swing.*;	// debug with JOptionPane.showMessageDialog
import java.net.*;

import com.bugsplatsoftware.client.gui.BugSplatDialog;
import com.bugsplatsoftware.client.gui.BugSplatProgress;
import com.bugsplatsoftware.client.gui.BugSplatDetails;
import com.bugsplatsoftware.client.util.BugSplatReport;
import com.bugsplatsoftware.client.util.BugSplatThread;
import com.bugsplatsoftware.client.util.BugSplatThreadGroup;

/**
 * BugSplat support for Java applications.
 * <p>
 * See the MyJavaCrasher sample for an implementation example.
 *
 */
public class BugSplat implements Runnable {

    // construct time - required
    private static String m_strDatabase = "";
    private static String m_strAppName = "";
    private static String m_strVersion = "";

    // additional info - optional
    private static String m_strStackTrace = "";
    private static String m_strDescription = "";
    private static String m_strMFA = "";
    private static String m_strStackKey = "";
    private static ArrayList m_additionalFiles = new ArrayList();

    // dialog data
    private static String m_strUserName = "";
    private static String m_strUserEmail = "";
    private static String m_strUserAddress = "";
    private static String m_strUserDescription = "";
    private static boolean m_bEnableAdditionalFiles = true;

    // IP address of local host
    private static String m_strIPAddress = "";

    // for server apps
    private static boolean m_bQuietMode = false;

    private static String m_strUserFile = "";
    private static String m_strStackFile = "";
    private static ArrayList m_requiredFiles = new ArrayList();
    private static String m_strZipFile = "";

    private static String m_strError = "";
    private static boolean m_reportCreated = false;
    private static boolean m_terminateApplication = false;

    private static Exception m_ex = null;

    /**
     * Initialize database, application and version. These are required for
     * validation when the report is sent to the BugSplat web site and for
     * organizational purposes when navigating the web site interactively.
     */
    public static void Init(
            String szDatabase, /* database on bugsplatsoftware.com*/
            String szAppName, /* application name as on file at bugsplatsoftware.com */
            String szVersion) /* version identifier, as supplied with PDB files on bugsplatsoftware.com*/ {
        m_strDatabase = szDatabase;
        m_strAppName = szAppName;
        m_strVersion = szVersion;

    }

    // currently, we handle the stack trace and MFA internally only
    private static void SetStackTrace(String stackTrace) {
        m_strStackTrace = stackTrace;
    }

    private static void SetMFA(String MFA) {
        m_strMFA = MFA;
    }

    private static void SetStackKey(String stackKey) {
        m_strStackKey = stackKey;
    }

    private static void GetIPAddressForLocalHost() {
        try {
            // returns the host name/ip address
            InetAddress inet = InetAddress.getLocalHost();

            // get the raw ip only
            byte[] raw = inet.getAddress();

			// java doesn't support unsigned types,
            // so we have to use some trickery
            int raw0 = 0xFF & (int) raw[0];
            int raw1 = 0xFF & (int) raw[1];
            int raw2 = 0xFF & (int) raw[2];
            int raw3 = 0xFF & (int) raw[3];

            m_strIPAddress = raw0 + "." + raw1 + "." + raw2 + "." + raw3;
        } catch (Exception e) {
            System.out.println("Exception in GetIPAddressForLocalHost: " + e.toString());
        }
    }

    private static String GetMFAFromException(Exception e) {

        // first, we need to backtrace...
        Throwable t = (Throwable) e;
        while (t.getCause() != null) {
            t = t.getCause();
        }

        // TODO: MFA should be module, file, address
        StackTraceElement stack[] = t.getStackTrace();
        if (stack.length > 0) {
            String className = stack[0].getClassName();
            String methodName = stack[0].getMethodName();
            int line = stack[0].getLineNumber();

            // changed from "::" to "."
            return className + "." + methodName + "(" + line + ")";
        }

        return "";
    }

    private static String GetStackKeyFromException(Exception e) {

        // first, we need to backtrace...
        Throwable t = (Throwable) e;
        while (t.getCause() != null) {
            t = t.getCause();
        }

        StackTraceElement stack[] = t.getStackTrace();
        if (stack.length > 0) {
            String className = stack[0].getClassName();
            String methodName = stack[0].getMethodName();
            int line = stack[0].getLineNumber();

            // added line
            return className + "." + methodName + "(" + line + ")";
        }

        return "";
    }

    /**
     * Provide a description of the crash that may be used to interactively
     * sort/filter crashes on the BugSplat web site.
     */
    public static void SetDescription(String description) {
        m_strDescription = description;
    }

    /**
     * Specify the path to an additional file to be included in the crash report
     * package sent to the BugSplat web site.
     */
    public static void AddAdditionalFile(String additionalFile) {
        m_additionalFiles.add(additionalFile);
    }

    /**
     * Set quiet mode. By default, a progress dialog is displayed as the crash
     * report is sent to the BugSplat web site. Calling this method with a true
     * argument inhibits the progress dialog.
     */
    public static void SetQuietMode(boolean quiet) {
        m_bQuietMode = quiet;
    }

    /**
     * Get quiet mode behavior set via SetQuietMode().
     */
    public static boolean GetQuietMode() {
        return m_bQuietMode;
    }

    /**
     * Set application termination behavior. By default, the application is not
     * terminated after an exception is processed by this class.
     */
    public static void SetTerminateApplication(boolean terminate) {
        m_terminateApplication = terminate;
    }

    /**
     * Get application termination behavior set via SetTerminateApplication().
     */
    public static boolean GetTerminateApplication() {
        return m_terminateApplication;
    }

    /**
     * Handle a caught exception with BugSplat. The crash report package will be
     * sent to the BugSplat web site.
     */
    public static void HandleException(Exception ex) {
        // save the exception
        m_ex = ex;

        // use a ThreadGroup so that we can handle uncaught exceptions
        ThreadGroup tg = new BugSplatThreadGroup("BugSplatThreadGroup");
        BugSplatThread t = new BugSplatThread(tg, new BugSplat());
        t.start();
    }

    /**
     * Used internally to send crash reports.
     */
    public void run() {
        System.out.println("Entering thread method...");

        // check if there is an exception to proceszs
        if (m_ex == null) {
            System.out.println("No exception...");
            return;
        }

        try {
            // show the main crash dialog
            boolean Result = BugSplat.ShowDialog();

            // check if user cancelled
            if (Result == true) {
                // create the progress dialog
                BugSplatProgress progress = new BugSplatProgress(BugSplat.GetQuietMode());

                // send the crash data
                BugSplat.SendData(m_ex, progress);

                // show the server response
                if (progress.TaskComplete != BugSplatProgress.taskSendingReport) {
                    if (BugSplat.GetQuietMode() == false) {
                        BugSplatDetails details = new BugSplatDetails(progress.ResultString);
                    }
                }

                System.out.println("Done.");

                // release the object to free the underlying memory and window handle
                progress.dispose();
            }

            if (BugSplat.GetTerminateApplication() == true) {
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("Exception in worker thread.");
        }

        // clear the exception
        m_ex = null;
    }

    private static boolean ShowDialog() {
        // create the temp files for the stack trace and user info
        CreateTempFiles();

        // create the main crash dialog
        BugSplatDialog dialog = new BugSplatDialog(m_requiredFiles, m_additionalFiles, m_bEnableAdditionalFiles, m_bQuietMode);
		// requestFocus();

        // get the values from the dialog
        if (m_bQuietMode == false) {
            m_strUserName = dialog.UserName;
            m_strUserEmail = dialog.UserEmail;
            m_strUserAddress = dialog.UserAddress;
            m_strUserDescription = dialog.UserDescription;
            m_bEnableAdditionalFiles = dialog.m_enableAdditionalFiles;
        }

        boolean result = dialog.Result;

        // release the object to free the underlying memory and window handle
        dialog.dispose();

        return result;
    }

    private static void SendData(Exception e, BugSplatProgress progress) {
        try {
            // get the IP address
            GetIPAddressForLocalHost();

            // get the MFA from the stack trace
            SetMFA(GetMFAFromException(e));

            // get the stack key from the stack trace
            SetStackKey(GetStackKeyFromException(e));

            // create a report from the stack trace
            CreateReport(e);

            // collect the info entered on the crash dialog
            CollectUserInfo();

            // zip up the required and additional files
            CreateZip();

            if (progress.Cancelled == true) {
                return;
            } else {
                progress.setTaskComplete(BugSplatProgress.taskCompilingReport);
            }

            // TODO: need to be able to cancel this
            boolean validated = BugSplatReport.AbleToSend(m_strDatabase, m_strAppName, m_strVersion);
            if (validated) {
                System.out.println("Able to send!");

                boolean accepted = BugSplatReport.AcceptReport(m_strMFA, m_strDatabase, m_strAppName, m_strVersion, m_strDescription);
                if (accepted) {
                    System.out.println("Accepted!");

                    if (progress.Cancelled == true) {
                        return;
                    } else {
                        progress.setTaskComplete(BugSplatProgress.taskContactingServer);
                    }

                    // TODO: need to be able to cancel this
                    BugSplatReport.PostDumpFile(m_strZipFile, m_strDatabase, m_strAppName, m_strVersion, "Java stack trace");

                    if (progress.Cancelled == true) {
                        return;
                    } else {
                        // report posted
                        progress.setTaskComplete(BugSplatProgress.taskSendingReport);

                        if (progress.QuietMode == false) {
                            BugSplatReport.HandlePostResponse();
                        }

                        progress.setVisible(false);
                    }
                } else {
                    // report not accepted
                    System.out.println("Report rejected!");

                    if (progress.Cancelled == true) {
                        return;
                    } else {
                        progress.setTaskComplete(BugSplatProgress.taskContactingServer);
                    }

                    // TODO: need to be able to cancel this
                    BugSplatReport.PretendPostDumpFile(m_strMFA, m_strDatabase, m_strAppName, m_strVersion, m_strDescription);

                    if (progress.Cancelled == true) {
                        return;
                    } else {
                        // report posted
                        progress.setTaskComplete(BugSplatProgress.taskSendingReport);

                        if (progress.QuietMode == false) {
                            BugSplatReport.HandlePretendPostResponse();
                        }

                        progress.setVisible(false);
                    }
                }
            } else {
                System.out.println("Unable to send!");
            }
        } catch (Exception e2) {
            System.out.println("Exception in SendData: " + e2.toString());
        }
    }

    private static void CreateTempFiles() {
        System.out.println("CreateTempFiles...");

        try {
            File temp1 = null;
            temp1 = File.createTempFile("user", ".xml");
            m_strUserFile = temp1.toString();

            File temp2 = null;
            temp2 = File.createTempFile("stack", ".jdmp");
            m_strStackFile = temp2.toString();

            m_requiredFiles.clear();
            m_requiredFiles.add(m_strUserFile);
            m_requiredFiles.add(m_strStackFile);
        } catch (IllegalArgumentException iae) {
            System.out.println("IllegalArgumentException in CreateTempFiles: " + iae.toString());
        } catch (IOException ioe) {
            System.out.println("IllegalArgumentException in CreateTempFiles: " + ioe.toString());
        } catch (SecurityException iae) {
            System.out.println("IllegalArgumentException in CreateTempFiles: " + iae.toString());
        }
    }

    private static void CollectUserInfo() {
        System.out.println("CollectUserInfo...");

        File temp = null;

        DateFormat dateFormat1 = new SimpleDateFormat("yy-MM-dd");
        DateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");

        String datetime = dateFormat1.format(new Date()) + " "
                + dateFormat2.format(new Date());

		// NOTE: we need to wrap most elements in CDATA sections
        // characters such as < and > will causes errors when user.xml is read
        // these can come from user input on the crash dialog or from the MFA
        try {
            // open the temp file
            temp = new File(m_strUserFile);

            // Write to temp file
            BufferedWriter out = new BufferedWriter(new FileWriter(temp));

            // no DOM or SAX needed!
            out.write("<BugReport>\n");

            out.write(" <BsSndRptVersion>");
            out.write("1.0");
            out.write("</BsSndRptVersion>\n");
            out.write(" <AppInfo>\n");

            // no CDATA - this is validated against illegal chars when they sign up
            out.write("  <Vendor>");
            out.write(m_strDatabase);
            out.write("</Vendor>\n");

            out.write("  <AppName>");
            out.write("<![CDATA[");
            out.write(m_strAppName);
            out.write("]]>");
            out.write("</AppName>\n");

            out.write("  <Version>");
            out.write("<![CDATA[");
            out.write(m_strVersion);
            out.write("]]>");
            out.write("</Version>\n");

            out.write("  <Description>");
            out.write("<![CDATA[");
            out.write(m_strDescription);
            out.write("]]>");
            out.write("</Description>\n");

            out.write(" </AppInfo>\n");

            out.write(" <ReportOccurence>\n");

            out.write("  <User>\n");

            out.write("   <Name>");
            out.write("<![CDATA[");
            out.write(m_strUserName);
            out.write("]]>");
            out.write("</Name>\n");

            out.write("   <Email>");
            out.write("<![CDATA[");
            out.write(m_strUserEmail);
            out.write("]]>");
            out.write("</Email>\n");

            out.write("   <Postal>");
            out.write("<![CDATA[");
            out.write(m_strUserAddress);
            out.write("]]>");
            out.write("</Postal>\n");

            out.write("   <IPAddress>");
            out.write("<![CDATA[");
            out.write(m_strIPAddress);
            out.write("]]>");
            out.write("</IPAddress>\n");

            out.write("  </User>\n");

            // no CDATA - we format and write this one
            out.write("  <DateTime>");
            out.write(datetime);
            out.write("</DateTime>\n");

            out.write("  <Description>");
            out.write("<![CDATA[");
            out.write(m_strUserDescription);
            out.write("]]>");
            out.write("</Description>\n");

            out.write("  <MFA>");
            out.write("<![CDATA[");
            out.write(m_strMFA);
            out.write("]]>");
            out.write("</MFA>\n");

            out.write("  <TotalAppUptime>");
            out.write("</TotalAppUptime>\n");

            out.write("  <TotalCrashCount>");
            out.write("</TotalCrashCount>\n");

            out.write(" </ReportOccurence>\n");

            out.write("</BugReport>\n");

            out.flush();
            out.close();
        } catch (IOException ioe) {
            System.out.println("IOException in CollectionUserInfo: " + ioe.toString());
        } catch (Exception e) {
            System.out.println("Exception in CollectionUserInfo: " + e.toString());
        }
    }

    private static void CreateReport(Exception ex) {
        System.out.println("CreateReport...");

        File temp = null;

        try {
            // Create temp file.
            temp = new File(m_strStackFile);

            // Write to temp file
            BufferedWriter out = new BufferedWriter(new FileWriter(temp));

            // first, we need to backtrace...
            Throwable t = (Throwable) ex;
            while (t.getCause() != null) {
                t = t.getCause();
            }

            StackTraceElement stack[] = t.getStackTrace();

			// stack[0] contains the method that created the exception.
            // stack[stack.length-1] contains the oldest method call.
            // Enumerate each stack element.
            for (int i = 0; i < stack.length; i++) {

                String filename = stack[i].getFileName();

                if (filename == null) {
                    // The source filename is not available
                }

                String className = stack[i].getClassName();
                String methodName = stack[i].getMethodName();
                String fileName = stack[i].getFileName();
                boolean isNativeMethod = stack[i].isNativeMethod();
                int lineNumber = stack[i].getLineNumber();

				// TODO: should we use toString?
                // out.write(className + "::" + methodName + "(" + lineNumber + ")\n");
                String msg = className + "." + methodName;

                // write the header information on the first pass through the loop
                if (i == 0) {
                    out.write("<report>\n");
                    out.write(" <process>\n");
                    out.write("  <exception>\n");
                    out.write("   <func><![CDATA[" + msg + "]]></func>\n");  // stack key
                    out.write("   <code><![CDATA[" + t.getMessage() + "]]></code>\n");

					// not sure how to handle the Source info, so for now I'm just sticking
                    // it in the explanation field
                    out.write("   <explanation><![CDATA[" + t.toString() + "]]></explanation>\n");
                    out.write("   <file>" + fileName + "</file>\n");
                    out.write("   <line>" + lineNumber + "</line>\n");
                    out.write("   <registers></registers>\n");
                    out.write("  </exception>\n");
                    out.write("  <modules numloaded=\"0\"></modules>\n");
                    out.write("  <threads count=\"1\">\n");
                    out.write("   <thread id=\"1\" current=\"yes\" event=\"yes\" framecount=\"1\">\n");
                }

                // write this frame of the stack
                out.write("    <frame>\n");
                out.write("     <symbol><![CDATA[" + msg + "]]></symbol>\n");
                out.write("     <arguments></arguments>\n");
                out.write("     <locals></locals>\n");
                out.write("     <file>" + fileName + "</file>\n");
                out.write("     <line>" + lineNumber + "</line>\n");
                out.write("    </frame>\n");
            }

            // write the xml epilogue and close the stream
            out.write("   </thread>\n");
            out.write("  </threads>\n");
            out.write(" </process>\n");
            out.write("</report>\n");

            out.close();

            m_reportCreated = true;

            // System.out.println("Stack trace: " + temp.toString());
        } catch (IOException ioe) {
            System.out.println("IOException in CreateReport: " + ioe.toString());
        } catch (Exception e) {
            System.out.println("Exception in CreateReport: " + e.toString());
        }
    }

    // http://www.devshed.com/c/a/Java/Zip-Meets-Java/1/
    private static void CreateZip() {
        System.out.println("CreateZip...");

        String zipFileName = "";

        try {
            DateFormat dateFormat1 = new SimpleDateFormat("MMddyy");
            DateFormat dateFormat2 = new SimpleDateFormat("HHmmss");

            String date = dateFormat1.format(new Date());
            String time = dateFormat2.format(new Date());
            zipFileName = System.getProperty("java.io.tmpdir") + m_strDatabase + "Crash_" + date + "_" + time + ".zip";

            byte[] buffer = new byte[18024];

            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
            m_strZipFile = zipFileName;

			//
            // stack file
            //
            System.out.println("Stackfile: " + m_strStackFile);

            // remove the path
            String entry = new File(m_strStackFile).getName();

            FileInputStream in = new FileInputStream(m_strStackFile);
            out.putNextEntry(new ZipEntry(entry));

            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            // close the current entry
            out.closeEntry();

            // close the current file input stream
            in.close();

            // delete it
            File temp1 = new File(m_strStackFile);
            temp1.delete();

			//
            // user file
            //
            System.out.println("Userfile: " + m_strUserFile);

            // remove the path
            entry = new File(m_strUserFile).getName();

            FileInputStream in2 = new FileInputStream(m_strUserFile);
            out.putNextEntry(new ZipEntry(entry));

            while ((len = in2.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            // close the current entry
            out.closeEntry();

            // close the current file input stream
            in2.close();

            // delete it
            File temp2 = new File(m_strUserFile);
            temp2.delete();

			//
            // additional files
            //
            if (m_bEnableAdditionalFiles == true) {
                int count = m_additionalFiles.size();
                for (int i = 0; i < count; i++) {
                    String additionalFile = (String) m_additionalFiles.get(i);
                    System.out.println("Userfile: " + additionalFile);

                    // remove the path
                    entry = new File(additionalFile).getName();

                    FileInputStream in3 = new FileInputStream(additionalFile);
                    out.putNextEntry(new ZipEntry(entry));

                    while ((len = in3.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }

                    // close the current entry
                    out.closeEntry();

                    // close the current file input stream
                    in3.close();
                }
            }

            // close the ZipOutPutStream
            out.close();

            System.out.println("Zipfile: " + m_strZipFile);
        } catch (IllegalArgumentException iae) {
            System.out.println("IllegalArgumentException in CreateZip: " + iae.toString());
        } catch (FileNotFoundException fnfe) {
            System.out.println("FileNotFoundException in CreateZip: " + fnfe.toString());
        } catch (IOException ioe) {
            System.out.println("IOException in CreateZip: " + ioe.toString());
        }
    }
}
