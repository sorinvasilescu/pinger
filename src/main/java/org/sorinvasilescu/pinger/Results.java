package org.sorinvasilescu.pinger;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class Results extends Observable {

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

    public void setPingResult(String name, String result, Boolean success) {
        /* all keys are populated on startup, so we can synchronize on the actual result value, not the whole hashmap */
        synchronized (pingResults.get(name)) {
            pingResults.get(name).setResult(result);
            pingResults.get(name).setSuccess(success);
        }
        this.setChanged();
        this.notifyObservers(name);
    }

    public void setTracerouteResult(String name, String result, Boolean success) {
        /* all keys are populated on startup, so we can synchronize on the actual result value, not the whole hashmap */
        synchronized (tracerouteResults.get(name)) {
            tracerouteResults.get(name).setResult(result);
            tracerouteResults.get(name).setSuccess(success);
        }
        this.setChanged();
        this.notifyObservers(name);
    }

    public void setHttpResult(String name, String result, Boolean success) {
        /* all keys are populated on startup, so we can synchronize on the actual result value, not the whole hashmap */
        synchronized (httpResults.get(name)) {
            httpResults.get(name).setResult(result);
            httpResults.get(name).setSuccess(success);
        }
        this.setChanged();
        this.notifyObservers(name);
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
