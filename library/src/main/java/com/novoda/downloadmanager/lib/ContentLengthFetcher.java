package com.novoda.downloadmanager.lib;

import android.util.Pair;

import com.novoda.downloadmanager.lib.logger.LLog;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import static java.net.HttpURLConnection.HTTP_OK;

class ContentLengthFetcher {

    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private static final int UNKNOWN_CONTENT_LENGTH = -1;
    private static final int TIMEOUT_MILLIS = (int) TimeUnit.SECONDS.toMillis(10);
    private static final String HEADER_USER_AGENT = "User-Agent";
    private static final String METHOD_HEAD = "HEAD";

    public long fetchContentLengthFor(FileDownloadInfo info) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(info.getUri()).openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(TIMEOUT_MILLIS);
            conn.setReadTimeout(TIMEOUT_MILLIS);
            conn.setRequestMethod(METHOD_HEAD);
            addRequestHeaders(info, conn);

            if (conn.getResponseCode() == HTTP_OK) {
                return getHeaderFieldLong(conn, HEADER_CONTENT_LENGTH, UNKNOWN_CONTENT_LENGTH);
            } else {
                return UNKNOWN_CONTENT_LENGTH;
            }

        } catch (IOException e) {
            LLog.e("Could not fetch content length.");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return UNKNOWN_CONTENT_LENGTH;
    }

    private static long getHeaderFieldLong(URLConnection conn, String field, long defaultValue) {
        try {
            return Long.parseLong(conn.getHeaderField(field));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void addRequestHeaders(FileDownloadInfo info, HttpURLConnection conn) {
        for (Pair<String, String> header : info.getHeaders()) {
            conn.addRequestProperty(header.first, header.second);
        }

        // Only splice in user agent when not already defined
        if (conn.getRequestProperty(HEADER_USER_AGENT) == null) {
            conn.addRequestProperty(HEADER_USER_AGENT, userAgent(info));
        }
    }

    private String userAgent(FileDownloadInfo info) {
        String userAgent = info.getUserAgent();
        if (userAgent == null) {
            return Constants.DEFAULT_USER_AGENT;
        }
        return userAgent;
    }
}
