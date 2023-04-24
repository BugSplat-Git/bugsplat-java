package com.bugsplat.http;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class BugSplatHttpClientFactoryImpl implements BugSplatHttpClientFactory {
    public CloseableHttpClient create() {
        return HttpClients.createDefault();
    }
}
