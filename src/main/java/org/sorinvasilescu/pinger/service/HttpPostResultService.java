package org.sorinvasilescu.pinger.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.http.client.HttpClient;
import org.sorinvasilescu.pinger.Result;
import org.sorinvasilescu.pinger.ResultView;
import org.sorinvasilescu.pinger.Results;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Observable;
import java.util.Observer;

public class HttpPostResultService implements Observer {
    private static final Logger logger = LogManager.getLogger();

    private String url;

    public HttpPostResultService(String url) {
        this.url = url;
    }

    @Override
    public void update(Observable o, Object arg) {
        logger.info("Notified by " + o.toString() + " with " + arg.toString());

        String host = (String) arg;

        ObjectMapper mapper = new ObjectMapper();
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        ResultView result = new ResultView();

        Result pingResult = Results.getInstance().getPingResult(host);
        if (pingResult != null) {
            result.setIcmp_ping(pingResult);
        }

        Result traceResult = Results.getInstance().getTracerouteResult(host);
        if (traceResult != null) {
            result.setTrace(traceResult);
        }

        Result httpResult = Results.getInstance().getHttpResult(host);
        if (httpResult != null) {
            result.setTcp_ping(httpResult);
        }

        StringEntity body;
        try {
            body = new StringEntity(mapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            logger.error("Json processing failed");
            return;
        } catch (UnsupportedEncodingException e) {
            logger.error("Unsupported encoding");
            return;
        }

        post.setEntity(body);
        try {
            client.execute(post);
        } catch (IOException e) {
            logger.error("Could not send POST to " + this.url);
        }
    }
}
