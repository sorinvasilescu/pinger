package org.sorinvasilescu.pinger.service;

import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class HttpService extends Thread {

    private String host;
    private Integer port;

    public HttpService(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        Undertow server = Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(new HttpHandler())
                .build();
        server.start();
    }

    private class HttpHandler implements io.undertow.server.HttpHandler {

        @Override
        public void handleRequest(HttpServerExchange exchange) {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/json");
            exchange.getResponseSender().send("Response");
        }
    }
}
