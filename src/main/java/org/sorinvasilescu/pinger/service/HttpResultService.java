package org.sorinvasilescu.pinger.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sorinvasilescu.pinger.Result;
import org.sorinvasilescu.pinger.ResultView;
import org.sorinvasilescu.pinger.Results;

public class HttpResultService extends Thread {

    private String host;
    private Integer port;

    private static final Logger logger = LogManager.getLogger();

    public HttpResultService(String host, Integer port) {
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
            String path = exchange.getRequestPath();
            if (path.contains("results")) {
                // path should be results/host where host was defined in properties
                String[] parts = path.split("results/");
                if (parts.length > 1) {
                    String host = parts[1];
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/json");

                    ObjectMapper mapper = new ObjectMapper();
                    ResultView result = new ResultView();

                    result.setHost(host);

                    Result pingResult = Results.getInstance().getPingResult(host);
                    if (pingResult != null) {
                        result.setIcmp_ping(pingResult);
                    }

                    Result traceResult = Results.getInstance().getTracerouteResult(host);
                    if (traceResult != null) {
                        result.setTrace(traceResult);
                    }

                    // send the result
                    try {
                        exchange.getResponseSender().send(mapper.writeValueAsString(result));
                    } catch (JsonProcessingException e) {
                        logger.error("Json processing failed");
                    }
                }
            }
        }
    }
}
