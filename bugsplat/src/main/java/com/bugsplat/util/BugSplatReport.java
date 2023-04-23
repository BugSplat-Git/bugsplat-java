//
// BugSplat integration code for Java applications.
// This class handles communication with the
// BugSplat server at www.BugSplatSoftware.com
//
// Copyright 2005 BugSplat, LLC.
//
package com.bugsplat.util;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import edu.stanford.ejalbert.BrowserLauncher;
import com.bugsplat.util.BugSplatFormPost;
import com.bugsplat.util.BugSplatTrustManager;

public class BugSplatReport {
 
    private static String postURL = "https://report.bugsplatsoftware.com:443/post/post_action.php";

    // TODO BG dep injection (spring?)
    private CloseableHttpClient httpClient;

    private String database;
    private String application;
    private String version;

    public BugSplatReport(String database, String application, String version, CloseableHttpClient httpClient) {
        this.database = database;
        this.application = application;
        this.version = version;
        this.httpClient = httpClient;
    }

    //
    // post the zip file to the BugSplat server
    //
    public boolean PostDumpFile(File zipfile, String description) throws Exception {

        System.out.println("Posting dump file...");
        
        String getCrashUploadUrl = String.format(
            "https://%s.bugsplat.com/api/getCrashUploadUrl?database=%s&appName=%s&appVersion=%s&crashPostSize=%s",
            this.database,
            this.database,
            this.application,
            this.version,
            zipfile.length()
        );
        HttpGet getCrashUploadUrlRequest = new HttpGet(getCrashUploadUrl);
        CloseableHttpResponse getCrashUploadUrlResponse = this.httpClient.execute(getCrashUploadUrlRequest);
        HttpEntity getCrashUploadUrlEntity = getCrashUploadUrlResponse.getEntity();
        String getCrashUploadUrlResponseBody = EntityUtils.toString(getCrashUploadUrlEntity);
        JSONObject getCrashUploadUrlResponseJson = new JSONObject(getCrashUploadUrlResponseBody);
        String crashUploadUrl = getCrashUploadUrlResponseJson.getString("url");

        System.out.println(crashUploadUrl);

        // List<NameValuePair> formData = new ArrayList<>();
        // formData.add(new BasicNameValuePair(("database"), this.database));

        return true;
    }

    //
    // save the response in BugSplatPost.log
    // and display the URL
    //
    public static boolean HandlePostResponse() {
        System.out.println("HandlePostResponse...");

        // TODO BG 
        
        //
        // open a browser window
        // http://browserlauncher.sourceforge.net/
        String infoUrl = "";
        try {
            // System.out.println("Launching browser: " + infoURL);
            edu.stanford.ejalbert.BrowserLauncher.openURL(infoUrl);
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return true;
    }
}