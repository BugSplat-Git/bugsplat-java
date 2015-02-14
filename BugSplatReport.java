//
// BugSplat integration code for Java applications.
// This class handles communication with the
// BugSplat server at www.BugSplatSoftware.com
//
// Copyright 2005 BugSplat, LLC.
//

import java.io.*;
import java.util.Vector;
import java.net.*;

// TODO: move this to the dialog module
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BugSplatReport
{
	// for local testing
	//private static String postURL = "http://localhost/post/post_action.php";
	//private static String ppstURL = "http://localhost/post/pretendpost_action.php";
    //private static String hostURL = "http://localhost/ws/VendorServices.php";
    //private static String testURL = "http://localhost/ws/ReportRejectionServices.php";
	//private static String infoURL = "http://localhost/browse/crashinfo.php";

	private static String postURL = "https://www.bugsplatsoftware.com:443/post/post_action.php";
	private static String ppstURL = "https://www.bugsplatsoftware.com:443/post/pretendpost_action.php";
 	private static String hostURL = "http://www.bugsplatsoftware.com/ws/VendorServices.php";
    private static String testURL = "http://www.bugsplatsoftware.com/ws/ReportRejectionServices.php";
	private static String infoURL = "http://www.bugsplatsoftware.com/browse/crashinfo.php";

	private static String serverPostResponse = "";
	private static String serverPretendPostResponse = "";

	//
	// post the zip file to the BugSplat server
	//
	public static boolean PostDumpFile(String zipfile,
	                                   String vendor,
	                                   String appName,
	                                   String version,
	                                   String description) throws Exception
    {
        try
        {
			System.out.println("Posting dump file...");

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
			out.writeFile("minidumpZip", "application/x-zip-compressed", new File(zipfile));

			out.close();

			// read response from server
			BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

			String line = "";
			serverPostResponse = "";
			while((line = in.readLine()) != null) {
				serverPostResponse = serverPostResponse + line;
				// System.out.println(line);
			}

			in.close();

			// System.out.println("Done.");

			return true;
        }
		catch (Exception e)
		{
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
	                                          String key) throws Exception
    {
        try
        {
			System.out.println("Pretend Posting dump file...");

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
			while((line = in.readLine()) != null) {
				serverPretendPostResponse = serverPretendPostResponse + line;
				//System.out.println(line);
			}

			in.close();

			// System.out.println("Done.");

			return true;
        }
		catch (Exception e)
		{
			System.out.println("Exception in PretendPostDumpFile: " + e.toString());
		}

        return false;
    }

	//
	// save the response in BugSplatPost.log
	// and display the URL
	//
	public static boolean HandlePostResponse()
	{
		System.out.println("HandlePostResponse...");
		//System.out.println(serverPostResponse);

		//
		// parse the results
		//
		String temp = "::::: DB_UPDATED #####";
		int nFind = serverPostResponse.indexOf(temp);
		if (nFind >= 0)
		{
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
				if( strStackKeyId.length() > 0 ) {

					int id = Integer.parseInt(strStackKeyId);

					// if (id >= 0)
					{
						infoURL += "?vendor=";	infoURL += strVendor;
						infoURL += "&app=";     infoURL += strApp;
						//infoUrl += "&version="; infoUrl += strVersion;

						//if( strDescription.length() > 0 ) {
						//	infoURL += "&key="; infoURL += strDescription;
						//}

						infoURL += "&id=";      infoURL += strStackKeyId;
						infoURL += "&row=";		infoURL += strRow;

						//
						// open a browser window
						// http://browserlauncher.sourceforge.net/

						try {
							// System.out.println("Launching browser: " + infoURL);
							BrowserLauncher.openURL(infoURL);
						}
						catch (Exception e) {
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
	public static boolean HandlePretendPostResponse()
	{
		System.out.println("HandlePretendPostResponse...");
		//System.out.println(serverPretendPostResponse);

		//
		// parse the results
		//
		String temp = "::::: START_STACKKEY #####";
		int nFind = serverPretendPostResponse.indexOf(temp);
		if (nFind >= 0)
		{
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
				if( strStackKeyId.length() > 0 ) {

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
						}
						catch (Exception e) {
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
	                                 String version) throws Exception
    {
		System.out.println("Checking BugSplat server...");

        try
        {
            // create the document
            javax.xml.parsers.DocumentBuilder xdb = org.apache.soap.util.xml.XMLParserUtils.getXMLDocBuilder();
			org.w3c.dom.Document doc = xdb.newDocument();
            if (doc == null) {
                throw new org.apache.soap.SOAPException (org.apache.soap.Constants.FAULT_CODE_CLIENT, "parsing error");
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
            msg.send (new java.net.URL(hostURL), URI, envelope);

            // receive response from the transport and dump it to the screen
            org.apache.soap.transport.SOAPTransport st = msg.getSOAPTransport ();
            BufferedReader br = st.receive ();

            String line = br.readLine();
		    while (line != null)
		    {
				//System.out.println (line);
				if (line.indexOf("true") != -1)
					return true;
				line = br.readLine();
		    }

            // System.out.println("Done.");
        }
        catch (Exception e)
        {
			System.out.println("Exception in AbleToSend: " + e.toString());
        }

        return false;
    }

	//
	// validate the vendor with the BugSplat server
	//
	public static boolean AcceptReport(String MFA,  // module, file, address
	                                   String vendor,
	                                   String app,
	                                   String version,
	                                   String key) throws Exception
    {
		System.out.println("Checking pending reports...");
		//System.out.println("MFA: " + MFA);
		//System.out.println("Stack key: " + key);

        try
        {
            // create the document
            javax.xml.parsers.DocumentBuilder xdb = org.apache.soap.util.xml.XMLParserUtils.getXMLDocBuilder();
			org.w3c.dom.Document doc = xdb.newDocument();
            if (doc == null) {
                throw new org.apache.soap.SOAPException (org.apache.soap.Constants.FAULT_CODE_CLIENT, "parsing error");
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
            msg.send (new java.net.URL(testURL), URI, envelope);

            // receive response from the transport and dump it to the screen
            org.apache.soap.transport.SOAPTransport st = msg.getSOAPTransport ();
            BufferedReader br = st.receive ();

			String line = "";
			while((line = br.readLine()) != null) {
				//System.out.println(line);

				if (line.indexOf("accept") != -1)
					return true;
			}

            // System.out.println("Done.");
        }
        catch (Exception e)
        {
			System.out.println("Exception in AcceptReport: " + e.toString());
        }

        return false;
    }
}

//
// Java Forums: http://forum.java.sun.com/thread.jspa?threadID=451245&messageID=2146623
//
class BugSplatFormPost
{
	private static final String NEWLINE = "\r\n";
	private static final String PREFIX = "--";
	private DataOutputStream out = null;
	private String boundary = null;

	public BugSplatFormPost(OutputStream os, String boundary) {
		if(os == null) {
			throw new IllegalArgumentException("Output stream is required.");
		}
		if(boundary == null || boundary.length() == 0) {
			throw new IllegalArgumentException("Boundary stream is required.");
		}
		this.out = new DataOutputStream(os);
		this.boundary = boundary;
	}

	public void writeField(String name, boolean value)
			throws java.io.IOException {
		writeField(name, new Boolean(value).toString());
	}

	public void writeField(String name, double value)
			throws java.io.IOException {
		writeField(name, Double.toString(value));
	}

	public void writeField(String name, float value)
			throws java.io.IOException {
		writeField(name, Float.toString(value));
	}

	public void writeField(String name, long value)
			throws java.io.IOException {
		writeField(name, Long.toString(value));
	}

	public void writeField(String name, int value)
			throws java.io.IOException {
		writeField(name, Integer.toString(value));
	}

	public void writeField(String name, short value)
			throws java.io.IOException {
		writeField(name, Short.toString(value));
	}

	public void writeField(String name, char value)
			throws java.io.IOException {
		writeField(name, new Character(value).toString());
	}

	public void writeField(String name, String value)
			throws java.io.IOException {
		if(name == null) {
			throw new IllegalArgumentException("Name cannot be null or empty.");
		}
		if(value == null) {
			value = "";
		}
		// write boundary
		out.writeBytes(PREFIX);
		out.writeBytes(boundary);
		out.writeBytes(NEWLINE);
		// write content header
		out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"");
		out.writeBytes(NEWLINE);
		out.writeBytes(NEWLINE);
		// write content
		out.writeBytes(value);
		out.writeBytes(NEWLINE);
		out.flush();
	}

	public void writeFile(String name, String mimeType, File file)
			throws java.io.IOException {
		if(file == null) {
			throw new IllegalArgumentException("File cannot be null.");
		}
		if(!file.exists()) {
			throw new IllegalArgumentException("File does not exist.");
		}
		if(file.isDirectory()) {
			throw new IllegalArgumentException("File cannot be a directory.");
		}
		writeFile(name, mimeType, file.getCanonicalPath(), new FileInputStream(file));
	}

	public void writeFile(String name, String mimeType,
			String fileName, InputStream is)
			throws java.io.IOException {
		if(is == null) {
			throw new IllegalArgumentException("Input stream cannot be null.");
		}
		if(fileName == null || fileName.length() == 0) {
			throw new IllegalArgumentException("File name cannot be null or empty.");
		}
		// write boundary
		out.writeBytes(PREFIX);
		out.writeBytes(boundary);
		out.writeBytes(NEWLINE);
		// write content header
		out.writeBytes("Content-Disposition: form-data; name=\"" + name +
			"\"; filename=\"" + fileName + "\"");
		out.writeBytes(NEWLINE);
		if(mimeType != null) {
			out.writeBytes("Content-Type: " + mimeType);
			out.writeBytes(NEWLINE);
		}
		out.writeBytes(NEWLINE);
		// write content
		byte[] data = new byte[1024];
		int r = 0;
		while((r = is.read(data, 0, data.length)) != -1) {
			out.write(data, 0, r);
		}
		// close input stream, but ignore any possible exception for it
		try {
			is.close();
		} catch(Exception e) {}
		out.writeBytes(NEWLINE);
		out.flush();
	}

	public void writeFile(String name, String mimeType,
			String fileName, byte[] data)
			throws java.io.IOException {
		if(data == null) {
			throw new IllegalArgumentException("Data cannot be null.");
		}
		if(fileName == null || fileName.length() == 0) {
			throw new IllegalArgumentException("File name cannot be null or empty.");
		}
		// write boundary
		out.writeBytes(PREFIX);
		out.writeBytes(boundary);
		out.writeBytes(NEWLINE);
		// write content header
		out.writeBytes("Content-Disposition: form-data; name=\"" + name +
			"\"; filename=\"" + fileName + "\"");
		out.writeBytes(NEWLINE);
		if(mimeType != null) {
			out.writeBytes("Content-Type: " + mimeType);
			out.writeBytes(NEWLINE);
		}
		out.writeBytes(NEWLINE);
		// write content
		out.write(data, 0, data.length);
		out.writeBytes(NEWLINE);
		out.flush();
	}

	public void flush() throws java.io.IOException {
		// out.flush();
	}

	public void close() throws java.io.IOException {
		// write final boundary
		out.writeBytes(PREFIX);
		out.writeBytes(boundary);
		out.writeBytes(PREFIX);
		out.writeBytes(NEWLINE);
		out.flush();
		out.close();
	}

	public String getBoundary() {
		return this.boundary;
	}

	public static URLConnection createConnection(URL url)
			throws java.io.IOException {
		URLConnection urlConn = url.openConnection();
		if(urlConn instanceof HttpURLConnection) {
			HttpURLConnection httpConn = (HttpURLConnection)urlConn;
			httpConn.setRequestMethod("POST");
		}
		urlConn.setDoInput(true);
		urlConn.setDoOutput(true);
		urlConn.setUseCaches(false);
		urlConn.setDefaultUseCaches(false);
		return urlConn;
	}

	public static String createBoundary() {
		return "--------------------" +
			Long.toString(System.currentTimeMillis(), 16);
	}

	public static String getContentType(String boundary) {
		return "multipart/form-data; boundary=" + boundary;
	}
}
