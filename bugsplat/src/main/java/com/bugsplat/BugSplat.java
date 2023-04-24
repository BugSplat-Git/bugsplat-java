//
// BugSplat integration code for Java applications.
// This class allows a Java application to post
// BugSplat reports on www.bugsplat.com
//
// Copyright 2014 BugSplat, LLC.
//
package com.bugsplat;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.text.*;

import com.bugsplat.api.BugSplatClient;
import com.bugsplat.api.BugSplatPostOptions;
import com.bugsplat.api.BugSplatPostResult;
import com.bugsplat.http.BugSplatHttpClientFactory;
import com.bugsplat.http.BugSplatHttpClientFactoryImpl;
import com.bugsplat.util.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.net.*;

import com.bugsplat.gui.BugSplatDialog;
import com.bugsplat.gui.BugSplatProgress;
import com.bugsplat.gui.BugSplatDetails;

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
    private static String m_strKey = "";
    private static String m_strNotes = "";
    private static String m_strMFA = "";
    private final static ArrayList<String> m_additionalFiles = new ArrayList<>();

    // dialog data
    private static String m_strUserName = "";
    private static String m_strUserEmail = "";
    private static String m_strUserDescription = "";
    private static boolean m_bEnableAdditionalFiles = true;


    // IP address of local host
    private static String m_strIPAddress = "";

    // for server apps
    private static boolean m_bQuietMode = false;

    private static String m_strUserFile = "";
    private static String m_strStackFile = "";
    private static final ArrayList<String> m_requiredFiles = new ArrayList<>();
    private static String m_strZipFile = "";
    private static boolean m_terminateApplication = false;

    private static Exception m_ex = null;

    private static final BugSplatHttpClientFactory httpClientFactory = new BugSplatHttpClientFactoryImpl();

    /**
     * Initialize database, application and version. These are required for
     * validation when the report is sent to the BugSplat website and for
     * organizational purposes when navigating the website interactively.
     */
    public static void init(
            String database, /* database on bugsplat.com */
            String application, /* application name */
            String version /* version identifier */
    ) {
        m_strDatabase = database;
        m_strAppName = application;
        m_strVersion = version;

    }

    private static void getIPAddressForLocalHost() {
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
            System.out.println("Exception in GetIPAddressForLocalHost: " + e);
        }
    }

    private static String getMFAFromException(Exception e) {

        // first, we need to backtrace...
        Throwable t = e;
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

    private static void setMFA(String MFA) {
        m_strMFA = MFA;
    }

    /**
     * Set the default value for the user description field
     * this value can be overwritten by the user in the BugSplat dialog
     */
    public static void setDescription(String description) {
        m_strUserDescription = description;
    }

    /**
     * Set the default value for the email field
     * this value can be overwritten by the user in the BugSplat dialog
     */
    public static void setEmail(String email) {
        m_strUserEmail = email;
    }

    /**
     * Provide an application key that may be used to load different
     * variations of the support response page.
     */
    public static void setKey(String key) {
        m_strKey = key;
    }

    /**
     * Provide notes associated with the crash report that can be edited
     * on the BugSplat website.
     */
    public static void setNotes(String notes) {
        m_strNotes = notes;
    }

    /**
     * Set the default value for the user field
     * this value can be overwritten by the user in the BugSplat dialog
     */
    public static void setUser(String user) {
        m_strUserName = user;
    }

    /**
     * Specify the path to an additional file to be included in the crash report
     * package sent to the BugSplat website.
     */
    public static void addAdditionalFile(String additionalFile) {
        m_additionalFiles.add(additionalFile);
    }

    /**
     * Set quiet mode. By default, a progress dialog is displayed as the crash
     * report is sent to the BugSplat website. Calling this method with a true
     * argument inhibits the progress dialog.
     */
    public static void setQuietMode(boolean quiet) {
        m_bQuietMode = quiet;
    }

    /**
     * Get quiet mode behavior set via SetQuietMode().
     */
    public static boolean getQuietMode() {
        return m_bQuietMode;
    }

    /**
     * Set application termination behavior. By default, the application is not
     * terminated after an exception is processed by this class.
     */
    public static void setTerminateApplication(boolean terminate) {
        m_terminateApplication = terminate;
    }

    /**
     * Get application termination behavior set via SetTerminateApplication().
     */
    public static boolean getTerminateApplication() {
        return m_terminateApplication;
    }

    /**
     * Handle a caught exception with BugSplat. The crash report package will be
     * sent to the BugSplat website.
     */
    public static void handleException(Exception ex) {
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

        // check if there is an exception to process
        if (m_ex == null) {
            System.out.println("No exception...");
            return;
        }

        try {
            // TODO BG only show if m_quietMode true?
            // show the main crash dialog
            boolean result = BugSplat.showDialog();

            // TODO BG only show if m_quietMode true?
            // check if user cancelled
            if (result) {
                // create the progress dialog
                BugSplatProgress progress = new BugSplatProgress(BugSplat.getQuietMode());

                // send the crash data
                BugSplat.sendData(m_ex, progress);

                // show the server response
                if (progress.TaskComplete != BugSplatProgress.taskSendingReport) {
                    if (!BugSplat.getQuietMode()) {
                        BugSplatDetails details = new BugSplatDetails(progress.ResultString);
                    }
                }

                System.out.println("Done.");

                // release the object to free the underlying memory and window handle
                progress.dispose();
            }

            if (BugSplat.getTerminateApplication()) {
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("Exception in worker thread.");
        }

        // clear the exception
        m_ex = null;
    }

    private static boolean showDialog() {
        // create the temp files for the stack trace and user info
        createTempFiles();

        // create the main crash dialog
        BugSplatDialog dialog = new BugSplatDialog(
                m_strUserName,
                m_strUserEmail,
                m_strUserDescription,
                m_requiredFiles,
                m_additionalFiles,
                m_bEnableAdditionalFiles,
                m_bQuietMode
        );
        // requestFocus();

        // get the values from the dialog
        if (!m_bQuietMode) {
            m_strUserName = dialog.UserName;
            m_strUserEmail = dialog.UserEmail;
            m_strUserDescription = dialog.UserDescription;
            m_bEnableAdditionalFiles = dialog.m_enableAdditionalFiles;
        }

        boolean result = dialog.Result;

        // release the object to free the underlying memory and window handle
        dialog.dispose();

        return result;
    }

    private static void sendData(Exception e, BugSplatProgress progress) {
        try {
            getIPAddressForLocalHost();

            setMFA(getMFAFromException(e));

            createReport(e);

            collectUserInfo();

            createZip();

            if (progress.Cancelled) {
                return;
            }
            
            progress.setTaskComplete(BugSplatProgress.taskCompilingReport);

            BugSplatPostResult postResult = uploadCrashZip();

            if (progress.Cancelled) {
                return;
            }

            progress.setTaskComplete(BugSplatProgress.taskSendingReport);

            if (!progress.QuietMode && postResult.success) {
                ShowSupportResponse(postResult.infoUrl);
            }

            progress.setVisible(false);
        } catch (Exception e2) {
            System.out.println("Exception in SendData: " + e2);
        }
    }

    private static void ShowSupportResponse(String infoUrl) {
        try {
            // System.out.println("Launching browser: " + infoURL);
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(infoUrl));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static BugSplatPostResult uploadCrashZip() throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        BugSplatClient report = new BugSplatClient(
                m_strDatabase,
                m_strAppName,
                m_strVersion,
                httpClientFactory
        );
        BugSplatPostOptions options = new BugSplatPostOptions();
        options.additionalFiles = m_additionalFiles;
        options.description = m_strUserDescription;
        options.email = m_strUserEmail;
        options.mfa = m_strMFA;
        options.notes = m_strNotes;
        options.key = m_strKey;
        options.user = m_strUserName;
        BugSplatPostResult result = report.PostDumpFile(new File(m_strZipFile), options);
        // TODO BG needed?
        client.close();
        return result;
    }

    private static void createTempFiles() {
        System.out.println("CreateTempFiles...");

        try {
            File temp1 = File.createTempFile("user", ".xml");
            m_strUserFile = temp1.toString();

            File temp2 = File.createTempFile("stack", ".jdmp");
            m_strStackFile = temp2.toString();

            m_requiredFiles.clear();
            m_requiredFiles.add(m_strUserFile);
            m_requiredFiles.add(m_strStackFile);
        } catch (IllegalArgumentException iae) {
            System.out.println("IllegalArgumentException in CreateTempFiles: " + iae);
        } catch (IOException ioe) {
            System.out.println("IOException in CreateTempFiles: " + ioe);
        } catch (SecurityException se) {
            System.out.println("SecurityException in CreateTempFiles: " + se);
        }
    }

    private static void collectUserInfo() {
        System.out.println("CollectUserInfo...");

        DateFormat dateFormat1 = new SimpleDateFormat("yy-MM-dd");
        DateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");

        String datetime = dateFormat1.format(new Date()) + " "
                + dateFormat2.format(new Date());

        // NOTE: we need to wrap most elements in CDATA sections
        // characters such as < and > will cause errors when user.xml is read
        // these can come from user input on the crash dialog or from the MFA
        try {
            // open the temp file
            File temp = new File(m_strUserFile);

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
            out.write(m_strKey);
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
            System.out.println("IOException in CollectionUserInfo: " + ioe);
        } catch (Exception e) {
            System.out.println("Exception in CollectionUserInfo: " + e);
        }
    }

    private static void createReport(Exception ex) {
        System.out.println("CreateReport...");

        try {
            // Create temp file.
            File temp = new File(m_strStackFile);

            // Write to temp file
            BufferedWriter out = new BufferedWriter(new FileWriter(temp));

            // first, we need to backtrace...
            Throwable t = ex;
            while (t.getCause() != null) {
                t = t.getCause();
            }

            StackTraceElement stack[] = t.getStackTrace();

            // stack[0] contains the method that created the exception.
            // stack[stack.length-1] contains the oldest method call.
            // Enumerate each stack element.
            for (int i = 0; i < stack.length; i++) {

                String filename = stack[i].getFileName();

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
                    out.write("   <explanation><![CDATA[" + t + "]]></explanation>\n");
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

            // System.out.println("Stack trace: " + temp.toString());
        } catch (IOException ioe) {
            System.out.println("IOException in CreateReport: " + ioe);
        } catch (Exception e) {
            System.out.println("Exception in CreateReport: " + e);
        }
    }

    // http://www.devshed.com/c/a/Java/Zip-Meets-Java/1/
    private static void createZip() {
        System.out.println("CreateZip...");

        try {
            DateFormat dateFormat1 = new SimpleDateFormat("MMddyy");
            DateFormat dateFormat2 = new SimpleDateFormat("HHmmss");

            String date = dateFormat1.format(new Date());
            String time = dateFormat2.format(new Date());
            String tmpDirName = System.getProperty("java.io.tmpdir");
            if (!tmpDirName.endsWith(File.separator)) {
              tmpDirName = tmpDirName + File.separator;
            }
            String zipFileName = tmpDirName + m_strDatabase + "Crash_" + date + "_" + time + ".zip";

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
            if (m_bEnableAdditionalFiles) {
                for (String additionalFile : m_additionalFiles) {
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
            System.out.println("IllegalArgumentException in CreateZip: " + iae);
        } catch (FileNotFoundException fnfe) {
            System.out.println("FileNotFoundException in CreateZip: " + fnfe);
        } catch (IOException ioe) {
            System.out.println("IOException in CreateZip: " + ioe);
        }
    }
}
