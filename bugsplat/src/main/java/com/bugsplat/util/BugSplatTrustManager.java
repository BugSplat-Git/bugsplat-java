//
// BugSplat integration code for Java applications.
// This class eliminates the need for certificates on each client.
//
// Copyright 2005 BugSplat, LLC.
//
package com.bugsplat.util;

import javax.net.ssl.X509TrustManager;

public class BugSplatTrustManager implements X509TrustManager
{
	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return null;
	}
	public void checkClientTrusted(
		java.security.cert.X509Certificate[] certs, String authType) {
	}
	public void checkServerTrusted(
		java.security.cert.X509Certificate[] certs, String authType) {
	}
}