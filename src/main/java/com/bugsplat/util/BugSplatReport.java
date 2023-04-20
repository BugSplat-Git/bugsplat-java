//
// BugSplat integration code for Java applications.
// This class handles communication with the
// BugSplat server at www.BugSplatSoftware.com
//
// Copyright 2005 BugSplat, LLC.
//
package com.bugsplat.util;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;
import java.net.URL;
import java.net.URLConnection;
import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.HttpsURLConnection;

import edu.stanford.ejalbert.BrowserLauncher;
import com.bugsplat.util.BugSplatFormPost;
import com.bugsplat.util.BugSplatTrustManager;

public class BugSplatReport {
    // for local testing
    //private static String postURL = "http://localhost/post/post_action.php";
    //private static String ppstURL = "http://localhost/post/pretendpost_action.php";
    //private static String hostURL = "http://localhost/ws/VendorServices.php";
    //private static String testURL = "http://localhost/ws/ReportRejectionServices.php";
    //private static String infoURL = "http://localhost/browse/crashinfo.php";

    private static String postURL = "https://report.bugsplatsoftware.com:443/post/post_action.php";
    private static String ppstURL = "https://report.bugsplatsoftware.com:443/post/pretendpost_action.php";
    private static String hostURL = "http://report.bugsplatsoftware.com/ws/VendorServices.php";
    private static String testURL = "http://report.bugsplatsoftware.com/ws/ReportRejectionServices.php";
    private static String infoURL = "https://www.bugsplatsoftware.com/browse/crashinfo.php";

    private static String serverPostResponse = "";
    private static String serverPretendPostResponse = "";

    //
    // post the zip file to the BugSplat server
    //
    public static boolean PostDumpFile(String zipfile,
            String vendor,
            String appName,
            String version,
            String description) throws Exception {
        try {
            System.out.println("Posting dump file...");

            // on Mac OSX, we will get a Validator Exception unless we disable certificates
            // http://lists.apple.com/archives/java-dev/2006/Feb/msg00052.html
            // http://www.javaworld.com/javatips/jw-javatip115.html
            // install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new BugSplatTrustManager()}, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // now we can access the https URL without having the certificate in the truststore
            URL url = new URL(postURL);

            // create a boundary string
            String boundary = BugSplatFormPost.createBoundary();
            URLConnection urlConn = BugSplatFormPost.createConnection(url);
            urlConn.setRequestProperty("Accept", "*/*");
            urlConn.setRequestProperty("Content-Type",
                    BugSplatFormPost.getContentType(boundary));

            // set some other request headers...
            urlConn.setRequestProperty("Connection", "Keep-Alive");
            urlConn.setRequestProperty("Cache-Control", "no-cache");

            // no need to connect cuz getOutputStream() does it
            BugSplatFormPost out = new BugSplatFormPost(urlConn.getOutputStream(), boundary);

            // write a text field element
            //out.writeField("minidumpZip", zipfile);
            // upload a file
            File upload = new File(zipfile);
            upload.deleteOnExit();
            out.writeFile("minidumpZip", "application/x-zip-compressed", upload);

            out.close();

            // read response from server
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

            String line = "";
            serverPostResponse = "";
            while ((line = in.readLine()) != null) {
                serverPostResponse = serverPostResponse + line;
                // System.out.println(line);
            }

            in.close();

            // System.out.println("Done.");
            return true;
        } catch (Exception e) {
            System.out.println("Exception in PostDumpFile: " + e.toString());
        }

        return false;
    }

    //
    // pretend to post to the BugSplat server
    //
    public static boolean PretendPostDumpFile(String MFA,
            String vendor,
            String appName,
            String version,
            String key) throws Exception {
        try {
            System.out.println("Pretend Posting dump file...");

            // on Mac OSX, we will get a Validator Exception unless we disable certificates
            // http://lists.apple.com/archives/java-dev/2006/Feb/msg00052.html
            // http://www.javaworld.com/javatips/jw-javatip115.html
            // install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new BugSplatTrustManager()}, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // now we can access the https URL without having the certificate in the truststore
            URL url = new URL(ppstURL);

            // create a boundary string
            String boundary = BugSplatFormPost.createBoundary();
            URLConnection urlConn = BugSplatFormPost.createConnection(url);
            urlConn.setRequestProperty("Accept", "*/*");
            urlConn.setRequestProperty("Content-Type",
                    BugSplatFormPost.getContentType(boundary));

            // set some other request headers...
            urlConn.setRequestProperty("Connection", "Keep-Alive");
            urlConn.setRequestProperty("Cache-Control", "no-cache");

            // no need to connect cuz getOutputStream() does it
            BugSplatFormPost out = new BugSplatFormPost(urlConn.getOutputStream(), boundary);

            // write a text field element
            out.writeField("mfa", MFA);
            out.writeField("vendor", vendor);
            out.writeField("app", appName);
            out.writeField("version", version);
            out.writeField("key", key);

            out.close();

            // read response from server
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

            String line = "";
            serverPretendPostResponse = "";
            while ((line = in.readLine()) != null) {
                serverPretendPostResponse = serverPretendPostResponse + line;
                //System.out.println(line);
            }

            in.close();

            // System.out.println("Done.");
            return true;
        } catch (Exception e) {
            System.out.println("Exception in PretendPostDumpFile: " + e.toString());
        }

        return false;
    }

    //
    // save the response in BugSplatPost.log
    // and display the URL
    //
    public static boolean HandlePostResponse() {
        System.out.println("HandlePostResponse...");
		//System.out.println(serverPostResponse);

        //
        // parse the results
        //
        String temp = "::::: DB_UPDATED #####";
        int nFind = serverPostResponse.indexOf(temp);
        if (nFind >= 0) {
            String strVendor = "";
            String strApp = "";
            String strRow = "";
            String strStackKeyId = "";

            // find some things to write to the error log...
            serverPostResponse = serverPostResponse.substring(nFind + temp.length());

            temp = "##";
            nFind = serverPostResponse.indexOf(temp);
            if (nFind >= 0) {
                strVendor = serverPostResponse.substring(0, nFind);

                serverPostResponse = serverPostResponse.substring(nFind + temp.length());
                nFind = serverPostResponse.indexOf(temp);
                if (nFind >= 0) {
                    strApp = serverPostResponse.substring(0, nFind);

                    serverPostResponse = serverPostResponse.substring(nFind + temp.length());
                    nFind = serverPostResponse.indexOf(temp);
                    if (nFind >= 0) {
                        strRow = serverPostResponse.substring(0, nFind);

                        serverPostResponse = serverPostResponse.substring(nFind + temp.length());
                        nFind = serverPostResponse.indexOf(temp);
                        if (nFind >= 0) {
                            strStackKeyId = serverPostResponse.substring(0, nFind);
                        }
                    }
                }

                //
                // build the URL
                //
                if (strStackKeyId.length() > 0) {

                    int id = Integer.parseInt(strStackKeyId);

                    // if (id >= 0)
                    {
                        infoURL += "?vendor=";
                        infoURL += strVendor;
                        infoURL += "&app=";
                        infoURL += strApp;
						//infoUrl += "&version="; infoUrl += strVersion;

                        //if( strDescription.length() > 0 ) {
                        //	infoURL += "&key="; infoURL += strDescription;
                        //}
                        infoURL += "&id=";
                        infoURL += strStackKeyId;
                        infoURL += "&row=";
                        infoURL += strRow;

                        //
                        // open a browser window
                        // http://browserlauncher.sourceforge.net/
                        try {
                            // System.out.println("Launching browser: " + infoURL);
                            edu.stanford.ejalbert.BrowserLauncher.openURL(infoURL);
                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                    }
                }
            }
        }
        return true;
    }

    //
    // save the response in BugSplatPost.log
    // and display the URL
    //
    public static boolean HandlePretendPostResponse() {
        System.out.println("HandlePretendPostResponse...");
		//System.out.println(serverPretendPostResponse);

        //
        // parse the results
        //
        String temp = "::::: START_STACKKEY #####";
        int nFind = serverPretendPostResponse.indexOf(temp);
        if (nFind >= 0) {
            String strStackKeyId = "";
            String strURL = "";

            // find some things to write to the error log...
            serverPretendPostResponse = serverPretendPostResponse.substring(nFind + temp.length());

            temp = "##";
            nFind = serverPretendPostResponse.indexOf(temp);
            if (nFind >= 0) {
                strStackKeyId = serverPretendPostResponse.substring(0, nFind);

                serverPretendPostResponse = serverPretendPostResponse.substring(nFind + temp.length());

                // extract the string to pass to crashInfo.php
                // \nInformation related to this crash can be found
                // <a href=/browse/crashInfo.php?vendor=Fred&version=1.0&key=Java stack trace&id=0>here</a>.                </td>
                temp = "?vendor";
                nFind = serverPretendPostResponse.indexOf(temp);
                if (nFind >= 0) {
                    temp = ">here";
                    int nEnd = serverPretendPostResponse.indexOf(temp);
                    strURL = serverPretendPostResponse.substring(nFind, nEnd);
                }

                //
                // build the URL
                //
                if (strStackKeyId.length() > 0) {

                    int id = Integer.parseInt(strStackKeyId);

                    // if (id >= 0)
                    {
                        infoURL += strURL;

                        //
                        // open a browser window
                        // http://browserlauncher.sourceforge.net/
                        try {
                            // System.out.println("Launching browser: " + infoURL);
                            BrowserLauncher.openURL(infoURL);
                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                    }
                }
            }
        }
        return true;
    }

    //
    // validate the vendor with the BugSplat server
    //
    public static boolean AbleToSend(String vendor,
            String appName,
            String version) throws Exception {
        System.out.println("Checking BugSplat server...");

        try {
            // create the document
            javax.xml.parsers.DocumentBuilder xdb = org.apache.soap.util.xml.XMLParserUtils.getXMLDocBuilder();
            org.w3c.dom.Document doc = xdb.newDocument();
            if (doc == null) {
                throw new org.apache.soap.SOAPException(org.apache.soap.Constants.FAULT_CODE_CLIENT, "parsing error");
            }

            // create the body element
            org.w3c.dom.Element element1 = doc.createElement("ValidateVendor");
            element1.setAttribute("xmlns", "urn:BugsplatVendorTests");
            org.w3c.dom.Element element2 = doc.createElement("vendorName");
            org.w3c.dom.Text element3 = doc.createTextNode(vendor);
            element1.appendChild(element2);
            element2.appendChild(element3);

            // create a vector for collecting the body elements
            Vector bodyElements = new Vector();
            bodyElements.add(element1);

            // create the SOAP envelope
            org.apache.soap.Envelope envelope = new org.apache.soap.Envelope();

            // create the SOAP body element
            org.apache.soap.Body body = new org.apache.soap.Body();
            body.setBodyEntries(bodyElements);

            // add the SOAP body element to the envelope
            envelope.setBody(body);

            // build the Message.
            org.apache.soap.messaging.Message msg = new org.apache.soap.messaging.Message();

            // send the message
            String URI = "";
            msg.send(new java.net.URL(hostURL), URI, envelope);

            // receive response from the transport and dump it to the screen
            org.apache.soap.transport.SOAPTransport st = msg.getSOAPTransport();
            BufferedReader br = st.receive();

            String line = br.readLine();
            while (line != null) {
                //System.out.println (line);
                if (line.indexOf("true") != -1) {
                    return true;
                }
                line = br.readLine();
            }

            // System.out.println("Done.");
        } catch (Exception e) {
            System.out.println("Exception in AbleToSend: " + e.toString());
        }

        return false;
    }

    //
    // validate the vendor with the BugSplat server
    //
    public static boolean AcceptReport(String MFA, // module, file, address
            String vendor,
            String app,
            String version,
            String key) throws Exception {
        System.out.println("Checking pending reports...");
        //System.out.println("MFA: " + MFA);
        //System.out.println("Stack key: " + key);

        try {
            // create the document
            javax.xml.parsers.DocumentBuilder xdb = org.apache.soap.util.xml.XMLParserUtils.getXMLDocBuilder();
            org.w3c.dom.Document doc = xdb.newDocument();
            if (doc == null) {
                throw new org.apache.soap.SOAPException(org.apache.soap.Constants.FAULT_CODE_CLIENT, "parsing error");
            }

            // create the body element
            org.w3c.dom.Element element1 = doc.createElement("ReportRejectionTest");
            element1.setAttribute("xmlns", "urn:BugsplatReportRejectionTests");

            // MFA
            org.w3c.dom.Element element2 = doc.createElement("MFA");
            org.w3c.dom.Text text2 = doc.createTextNode(MFA);
            element1.appendChild(element2);
            element2.appendChild(text2);

            // vendor
            org.w3c.dom.Element element3 = doc.createElement("vendor");
            org.w3c.dom.Text text3 = doc.createTextNode(vendor);
            element1.appendChild(element3);
            element3.appendChild(text3);

            // app
            org.w3c.dom.Element element4 = doc.createElement("app");
            org.w3c.dom.Text text4 = doc.createTextNode(app);
            element1.appendChild(element4);
            element4.appendChild(text4);

            // version
            org.w3c.dom.Element element5 = doc.createElement("version");
            org.w3c.dom.Text text5 = doc.createTextNode(version);
            element1.appendChild(element5);
            element5.appendChild(text5);

            // key
            org.w3c.dom.Element element6 = doc.createElement("key");
            org.w3c.dom.Text text6 = doc.createTextNode(key);
            element1.appendChild(element6);
            element6.appendChild(text6);

            // create a vector for collecting the body elements
            Vector bodyElements = new Vector();
            bodyElements.add(element1);

            // create the SOAP envelope
            org.apache.soap.Envelope envelope = new org.apache.soap.Envelope();

            // create the SOAP body element
            org.apache.soap.Body body = new org.apache.soap.Body();
            body.setBodyEntries(bodyElements);

            // add the SOAP body element to the envelope
            envelope.setBody(body);

            // build the Message.
            org.apache.soap.messaging.Message msg = new org.apache.soap.messaging.Message();

            // send the message
            String URI = "";
            msg.send(new java.net.URL(testURL), URI, envelope);

            // receive response from the transport and dump it to the screen
            org.apache.soap.transport.SOAPTransport st = msg.getSOAPTransport();
            BufferedReader br = st.receive();

            String line = "";
            while ((line = br.readLine()) != null) {
                //System.out.println(line);

                if (line.indexOf("accept") != -1) {
                    return true;
                }
            }

            // System.out.println("Done.");
        } catch (Exception e) {
            System.out.println("Exception in AcceptReport: " + e.toString());
        }

        return false;
    }
}