package com.bugsplat.http;

import org.apache.http.impl.client.CloseableHttpClient;

public interface BugSplatHttpClientFactory {
    CloseableHttpClient create();
}
