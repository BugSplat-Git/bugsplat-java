//
// BugSplat integration code for Java applications.
// This class handles communication with the
// BugSplat server at www.BugSplatSoftware.com
//
// Copyright 2005 BugSplat, LLC.
//
package com.bugsplatsoftware.client.util;

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

            // TODO BG implement

            // on Mac OSX, we will get a Validator Exception unless we disable certificates
            // http://lists.apple.com/archives/java-dev/2006/Feb/msg00052.html
            // http://www.javaworld.com/javatips/jw-javatip115.html
            // install the all-trusting trust manager
            // SSLContext sc = SSLContext.getInstance("SSL");
            // sc.init(null, new TrustManager[]{new BugSplatTrustManager()}, new java.security.SecureRandom());
            // HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // // now we can access the https URL without having the certificate in the truststore
            // URL url = new URL(postURL);

            // // create a boundary string
            // String boundary = BugSplatFormPost.createBoundary();
            // URLConnection urlConn = BugSplatFormPost.createConnection(url);
            // urlConn.setRequestProperty("Accept", "*/*");
            // urlConn.setRequestProperty("Content-Type",
            //         BugSplatFormPost.getContentType(boundary));

            // // set some other request headers...
            // urlConn.setRequestProperty("Connection", "Keep-Alive");
            // urlConn.setRequestProperty("Cache-Control", "no-cache");

            // // no need to connect cuz getOutputStream() does it
            // BugSplatFormPost out = new BugSplatFormPost(urlConn.getOutputStream(), boundary);

            // // write a text field element
            // //out.writeField("minidumpZip", zipfile);
            // // upload a file
            // File upload = new File(zipfile);
            // upload.deleteOnExit();
            // out.writeFile("minidumpZip", "application/x-zip-compressed", upload);

            // out.close();

            // // read response from server
            // BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

            // String line = "";
            // serverPostResponse = "";
            // while ((line = in.readLine()) != null) {
            //     serverPostResponse = serverPostResponse + line;
            //     // System.out.println(line);
            // }

            // in.close();

            // System.out.println("Done.");
            return true;
        } catch (Exception e) {
            System.out.println("Exception in PostDumpFile: " + e.toString());
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

        // TODO BG implement

        // //
        // // parse the results
        // //
        // String temp = "::::: DB_UPDATED #####";
        // int nFind = serverPostResponse.indexOf(temp);
        // if (nFind >= 0) {
        //     String strVendor = "";
        //     String strApp = "";
        //     String strRow = "";
        //     String strStackKeyId = "";

        //     // find some things to write to the error log...
        //     serverPostResponse = serverPostResponse.substring(nFind + temp.length());

        //     temp = "##";
        //     nFind = serverPostResponse.indexOf(temp);
        //     if (nFind >= 0) {
        //         strVendor = serverPostResponse.substring(0, nFind);

        //         serverPostResponse = serverPostResponse.substring(nFind + temp.length());
        //         nFind = serverPostResponse.indexOf(temp);
        //         if (nFind >= 0) {
        //             strApp = serverPostResponse.substring(0, nFind);

        //             serverPostResponse = serverPostResponse.substring(nFind + temp.length());
        //             nFind = serverPostResponse.indexOf(temp);
        //             if (nFind >= 0) {
        //                 strRow = serverPostResponse.substring(0, nFind);

        //                 serverPostResponse = serverPostResponse.substring(nFind + temp.length());
        //                 nFind = serverPostResponse.indexOf(temp);
        //                 if (nFind >= 0) {
        //                     strStackKeyId = serverPostResponse.substring(0, nFind);
        //                 }
        //             }
        //         }

        //         //
        //         // build the URL
        //         //
        //         if (strStackKeyId.length() > 0) {

        //             int id = Integer.parseInt(strStackKeyId);

        //             // if (id >= 0)
        //             {
        //                 infoURL += "?vendor=";
        //                 infoURL += strVendor;
        //                 infoURL += "&app=";
        //                 infoURL += strApp;
		// 				//infoUrl += "&version="; infoUrl += strVersion;

        //                 //if( strDescription.length() > 0 ) {
        //                 //	infoURL += "&key="; infoURL += strDescription;
        //                 //}
        //                 infoURL += "&id=";
        //                 infoURL += strStackKeyId;
        //                 infoURL += "&row=";
        //                 infoURL += strRow;

        //                 //
        //                 // open a browser window
        //                 // http://browserlauncher.sourceforge.net/
        //                 try {
        //                     // System.out.println("Launching browser: " + infoURL);
        //                     edu.stanford.ejalbert.BrowserLauncher.openURL(infoURL);
        //                 } catch (Exception e) {
        //                     System.out.println(e.toString());
        //                 }
        //             }
        //         }
        //     }
        // }
        return true;
    }
}
