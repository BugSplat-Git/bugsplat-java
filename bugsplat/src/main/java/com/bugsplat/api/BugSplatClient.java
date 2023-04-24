//
// BugSplat integration code for Java applications.
// This class handles communication with the
// BugSplat server at www.BugSplatSoftware.com
//
// Copyright 2005 BugSplat, LLC.
//
package com.bugsplat.api;

import com.bugsplat.http.BugSplatHttpClientFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class BugSplatClient {

    private final BugSplatHttpClientFactory httpClientFactory;

    private final String database;
    private final String application;
    private final String version;

    final static int crashTypeId = 4;

    // TODO BG http client factory
    public BugSplatClient(
            String database,
            String application,
            String version,
            BugSplatHttpClientFactory httpClientFactory
    ) {
        this.database = database;
        this.application = application;
        this.version = version;
        this.httpClientFactory = httpClientFactory;
    }

    //
    // post the zip file to the BugSplat server
    //
    public BugSplatPostResult PostDumpFile(File crashZip, BugSplatPostOptions options) throws Exception {
        CloseableHttpClient httpClient = this.httpClientFactory.create();

        System.out.println("Getting pre-signed url...");

        String crashUploadUrl = getCrashUploadUrl(httpClient, crashZip);

        System.out.println("Upload crash zip file...");

        HttpResponse crashUploadResponse = uploadCrashZipToS3(httpClient, crashZip, crashUploadUrl);

        System.out.println("Committing crash to BugSplat...");

        String md5 = getETagFromResponseUnquoted(crashUploadResponse);
        BugSplatPostResult result = commitS3CrashUpload(
                httpClient,
                crashUploadUrl,
                md5,
                options
        );

        System.out.println("Upload complete!");

        httpClient.close();

        return result;
    }

    private static String getETagFromResponseUnquoted(HttpResponse crashUploadResponse) {
        String eTag = crashUploadResponse.getHeaders("ETag")[0].getValue();
        String eTagUnquoted = eTag.replace("\"", "");
        return eTagUnquoted;
    }

    private BugSplatPostResult commitS3CrashUpload(HttpClient httpClient, String crashUploadUrl, String md5, BugSplatPostOptions options) throws IOException {
        String commitS3CrashUrl = String.format("https://%s.bugsplat.com/api/commitS3CrashUpload", this.database);
        HttpPost commitCrashRequest = new HttpPost(commitS3CrashUrl);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addTextBody("database", this.database)
                .addTextBody("appName", this.application)
                .addTextBody("appVersion", this.version)
                .addTextBody("md5", md5)
                .addTextBody("s3Key", crashUploadUrl)
                .addTextBody("crashTypeId", String.valueOf(crashTypeId));

        if (!options.description.isBlank()) {
            builder.addTextBody("description", options.description);
        }
        if (!options.email.isBlank()) {
            builder.addTextBody("email", options.email);
        }
        if (!options.key.isBlank()) {
            builder.addTextBody("appKey", options.key);
        }
        if (!options.notes.isBlank()) {
            builder.addTextBody("notes", options.notes);
        }
        if (!options.user.isBlank()) {
            builder.addTextBody("user", options.user);
        }

        HttpEntity entity = builder.build();
        commitCrashRequest.setEntity(entity);
        HttpResponse commitCrashResponse = httpClient.execute(commitCrashRequest);

        HttpEntity getCrashUploadUrlEntity = commitCrashResponse.getEntity();
        String getCrashUploadUrlResponseBody = EntityUtils.toString(getCrashUploadUrlEntity);
        JSONObject getCrashUploadUrlResponseJson = new JSONObject(getCrashUploadUrlResponseBody);

        boolean success = false;
        String infoUrl = "";
        if (getCrashUploadUrlResponseJson.has("infoUrl")) {
            success = true;
            infoUrl = getCrashUploadUrlResponseJson.getString("infoUrl");
        }

        return new BugSplatPostResult(success, infoUrl);
    }

    private HttpResponse uploadCrashZipToS3(HttpClient httpClient, File crashZip, String crashUploadUrl) throws IOException {
        HttpPut uploadRequest = new HttpPut(crashUploadUrl);
        FileEntity reqEntity = new FileEntity(crashZip, "application/octet-stream");
        reqEntity.setContentType("application/octet-stream");
        uploadRequest.setEntity(reqEntity);
        HttpResponse response = httpClient.execute(uploadRequest);
        return response;
    }

    private String getCrashUploadUrl(HttpClient httpClient, File zipfile) throws IOException {
        String getCrashUploadUrl = String.format(
            "https://%s.bugsplat.com/api/getCrashUploadUrl?database=%s&appName=%s&appVersion=%s&crashPostSize=%s",
            this.database,
            this.database,
            this.application,
            this.version,
            zipfile.length()
        );
        HttpGet getCrashUploadUrlRequest = new HttpGet(getCrashUploadUrl);
        HttpResponse getCrashUploadUrlResponse = httpClient.execute(getCrashUploadUrlRequest);
        HttpEntity getCrashUploadUrlEntity = getCrashUploadUrlResponse.getEntity();
        String getCrashUploadUrlResponseBody = EntityUtils.toString(getCrashUploadUrlEntity);
        JSONObject getCrashUploadUrlResponseJson = new JSONObject(getCrashUploadUrlResponseBody);
        String crashUploadUrl = getCrashUploadUrlResponseJson.getString("url");
        return crashUploadUrl;
    }
}