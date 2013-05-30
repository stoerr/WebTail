/*
 * Copyright (c) 2013 T-Systems Multimedia Solutions GmbH Dresden
 * Riesaer Str. 7, D-01129 Dresden, Germany
 * All rights reserved.
 *
 * WebTail.java created by hps at 29.05.2013
 */
package net.stoerr.webtail;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Simplest possible implementation of tail -F for a file on the web : retrieves endlessly the lines that are new in a URL.
 * Arguments: url proxyhost proxyport ; arguments proxyhost and proxyport are optional.
 * Done since http://www.jibble.org/webtail/ failed with CIT2 for some unknown reason.
 *
 * @author hps
 * @since 13.3 , 29.05.2013
 */
public class WebTail {

    private static final int SLEEPTIME = 5000;

    private final String url;
    private final HttpClient httpClient = new DefaultHttpClient();
    private long lastread = Long.MAX_VALUE;
    private byte[] buf = new byte[65536];

    public static void main(String[] args) throws Exception {
        new WebTail(args).run();
    }

    public WebTail(String[] args) {
        url = args[0];
        if (1 < args.length) {
            final HttpHost proxy = new HttpHost(args[1], Integer.valueOf(args[2]));
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
    }

    private void run() throws Exception {
        System.out.println("Tailing URL " + url + " starting from " + retrievesize());
        while (true) {
            printNewLoglines();
            Thread.sleep(SLEEPTIME);
        }
    }

    private void printNewLoglines() throws IOException {
        long currentsize = retrievesize();
        if (currentsize <= lastread)
            return;
        HttpGet get = new HttpGet(url);
        get.addHeader("Range", "bytes=" + lastread + "-" + currentsize);
        HttpResponse response = httpClient.execute(get);
        if (HttpStatus.SC_PARTIAL_CONTENT != response.getStatusLine().getStatusCode())
            throw new IllegalStateException("Unexpected status " + response.getStatusLine() + " in \n" + response);
        writeReceivedContentpart(response);
        lastread = currentsize;
    }

    private void writeReceivedContentpart(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        InputStream stream = entity.getContent();
        for (int read; (read = stream.read(buf)) > 0;) {
            System.out.write(buf, 0, read);
        }
    }

    private long retrievesize() throws IOException {
        HttpHead head = new HttpHead(url);
        HttpResponse response = httpClient.execute(head);
        Header acceptRanges = response.getFirstHeader("Accept-Ranges");
        if (null == acceptRanges || !acceptRanges.getValue().contains("bytes"))
            throw new IllegalStateException("Ranges not supported in\n" + response);
        return getContentLength(response);
    }

    private long getContentLength(HttpResponse response) {
        Header contentLength = response.getFirstHeader("Content-Length");
        long contentLengthValue = Long.valueOf(contentLength.getValue());
        if (contentLengthValue < lastread) {
            lastread = contentLengthValue;
        }
        return contentLengthValue;
    }
}
