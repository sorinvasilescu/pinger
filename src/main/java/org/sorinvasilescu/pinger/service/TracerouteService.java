package org.sorinvasilescu.pinger.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sorinvasilescu.pinger.Results;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TracerouteService extends Thread {

    private final Logger logger = LogManager.getLogger();

    private String name;

    public TracerouteService(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        Process process;
        InputStreamReader isr;
        BufferedReader br;
        String line;
        StringBuilder result;

        while (true) {
            try {
                result = new StringBuilder();
                process = new ProcessBuilder("tracert", name).start();
                isr = new InputStreamReader(process.getInputStream());
                br = new BufferedReader(isr);

                while ((line = br.readLine()) != null) {
                    result.append(line).append("\n");
                }

                Boolean success = isSuccessful(process.exitValue(), result.toString());

                Results.getInstance().addTracerouteResult(name, result.toString(), success);
                logger.warn(result.toString());
            } catch (IOException e) {
                logger.error("Could not execute command: tracert " + name + "\nCause: " + e.getMessage());
            }
        }
    }

    private Boolean isSuccessful(int exitValue, String result) {
        // TODO: replace this with result parsing
        return ( exitValue == 0 );
    }
}
