package org.sorinvasilescu.pinger.service;

import com.sun.jndi.toolkit.url.Uri;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sorinvasilescu.pinger.Results;

import java.io.IOException;
import java.net.MalformedURLException;

public class HttpCheckService extends Thread {

    private final Logger logger = LogManager.getLogger();

    private String url;
    private Integer delay;
    private Integer timeout;
    private String host;

    public HttpCheckService(String url, Integer delay, Integer timeout) {
        this.url = url;
        this.delay = delay;
        this.timeout = timeout;
        try {
            Uri uri = new Uri(url);
            this.host = uri.getHost();
        } catch (MalformedURLException e) {
            // do nothing, this should have been already thrown somewhere else
        }
    }

    public void run() {
        while (true) {
            try {
                HttpResponse response = httpRequest(url, timeout);
                Integer code = response.getStatusLine().getStatusCode();
                StringBuilder result = new StringBuilder();

                for (Header header : response.getAllHeaders()) {
                    result.append(header.toString());
                }

                Boolean success = isSuccessful(code, result.toString());
                Results.getInstance().setHttpResult(host, result.toString(), success);
                logger.warn(result);
            } catch (Exception e) {
                logger.error("Failed to send HTTP request to " + url + e);
            }

            try {
                sleep(delay * 1000);
            } catch (InterruptedException e) {
                logger.error("Interrupted");
            }
        }
    }

    private HttpResponse httpRequest(String url, Integer timeout) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpHead request = new HttpHead(url);
        request.setConfig(RequestConfig
                            .custom()
                            .setConnectTimeout(timeout*1000)
                            .setConnectionRequestTimeout(timeout*1000)
                            .setSocketTimeout(timeout*1000)
                            .build());
        try {
            return client.execute(request);
        } catch (IOException e) {
            return null;
        }
    }

    private Boolean isSuccessful(int code, String body) {
        return ( code == 200 );
    }
}
