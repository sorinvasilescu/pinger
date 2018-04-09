package org.sorinvasilescu.pinger;

import java.util.HashMap;
import java.util.Map;

public class Results {

    private final Map<String,Result> pingResults = new HashMap<>();
    private final Map<String,Result> tracerouteResults = new HashMap<>();
    private final Map<String,Result> httpResults = new HashMap<>();
    private static Results instance;

    private Results() {}

    public static Results getInstance() {
        if (instance == null) {
            instance = new Results();
        }
        return instance;
    }

    public void addPingResult(String name, String result, Boolean success) {
        /* all keys are populated on startup, so we can synchronize on the actual result value, not the whole hashmap */
        Result pingResult = pingResults.get(name);
        synchronized (pingResult) {
            pingResult.setResult(result);
            pingResult.setSuccess(success);
        }
    }

    public void addTracerouteResult(String name, String result, Boolean success) {
        /* all keys are populated on startup, so we can synchronize on the actual result value, not the whole hashmap */
        Result traceRouteResult = tracerouteResults.get(name);
        synchronized (traceRouteResult) {
            traceRouteResult.setResult(result);
            traceRouteResult.setSuccess(success);
        }
    }

    public void addHttpResult(String name, String result, Boolean success) {
        /* all keys are populated on startup, so we can synchronize on the actual result value, not the whole hashmap */
        Result httpResult = httpResults.get(name);
        synchronized (httpResult) {
            httpResult.setResult(result);
            httpResult.setSuccess(success);
        }
    }

    public Result getPingResult(String name) {
        /* all keys are populated at startup, so we can do without synchronization on the hashmap */
        if (pingResults.containsKey(name)) {
            synchronized (pingResults.get(name)) {
                return pingResults.get(name);
            }
        }
        return null;
    }

    public Result getTracerouteResult(String name) {
        /* all keys are populated at startup, so we can do without synchronization on the hashmap */
        if (tracerouteResults.containsKey(name)) {
            synchronized (tracerouteResults.get(name)) {
                return tracerouteResults.get(name);
            }
        }
        return null;
    }

    public Result getHttpResult(String name) {
        /* all keys are populated at startup, so we can do without synchronization on the hashmap */
        if (httpResults.containsKey(name)) {
            synchronized (httpResults.get(name)) {
                return httpResults.get(name);
            }
        }
        return null;
    }

    public void addHostname(String name) {
        synchronized (pingResults) {
            pingResults.put(name, new Result());
            tracerouteResults.put(name, new Result());
            httpResults.put(name, new Result());
        }
    }

    public Boolean hasHostName(String name) {
        synchronized (pingResults) {
            /* checking just one hashmap is sufficient because no outside access is allowed
               and addHostName method adds values to all 3 hashmaps at the same time */
            return pingResults.containsKey(name);
        }
    }
}
