package org.sorinvasilescu.pinger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.sorinvasilescu.pinger.service.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class Main {

    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        logger.info("Init started");
        if (!loadProperties()) {
            logger.error("Could not load properties file. Exiting.");
            return;
        }
        logger.info("Init finished");
    }

    private static boolean loadProperties() {
        Properties properties = new Properties();
        try {
            // try to load config file from same folder as jar file
            properties.load( new FileInputStream("./config.properties") );
        } catch (IOException e) {
            logger.warn("Did not find external config file. Falling back to internal config.");
            try {
                // fall back to internal config file
                properties.load( Main.class.getClassLoader().getResourceAsStream("config.properties") );
            } catch (IOException e1) {
                logger.error("Did not find internal config file.");
                return false;
            }
        }

        // log to file if filename are present in the config
        if (properties.containsKey("logPath")) {
            addFileLogger(properties.getProperty("logPath"));
        }

        // start web server if host and port are present in the config
        if (properties.containsKey("host") && properties.containsKey("port")) {
            addWebServer(properties);
        }

        // if there are hosts to be pinged
        if (properties.containsKey("pingAndTraceHosts")) {
            addPingAndTraceroute(properties);
        }

        // if there are urls to be checked via HTTP
        if (
                properties.containsKey("httpCheckUrls")
                && properties.containsKey("httpDelays")
                && properties.containsKey("httpTimeouts")
            ) {
            addHttpCheckers(properties);
        }

        // if there is a post URL
        if (properties.containsKey("postURL")) {
            HttpPostResultService postService = new HttpPostResultService(properties.getProperty("postURL"));
            Results.getInstance().addObserver(postService);
        }

        return true;
    }

    private static void addFileLogger(String logPath) {
        // get context and configs
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        /*
         * note: this might have been done by creating a different logger altogether
         * but for our use case, I think this is simpler and sufficient
         */
        final LoggerConfig rootConfig = config.getRootLogger();

        // create layout
        Layout<? extends Serializable> layout = PatternLayout.newBuilder()
                            .withPattern("%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n")
                            .build();

        // create appender
        Appender appender = FileAppender.newBuilder()
                .withFileName(logPath)
                .withAppend(true)
                .withLayout(layout)
                .withName("File")
                .build();
        appender.start();

        // add appender to root logger config
        config.addAppender(appender);
        rootConfig.addAppender(appender, Level.WARN, null);
        ctx.updateLoggers();

        logger.info("Loggers configured");
    }

    private static void addWebServer(Properties properties) {
        String host = properties.getProperty("host");
        Integer port = Integer.valueOf(properties.getProperty("port"));
        HttpResultServer httpResultServer = new HttpResultServer(host,port);
        httpResultServer.run();
    }

    private static void addPingAndTraceroute(Properties properties) {
        String pingHosts = properties.getProperty("pingAndTraceHosts");
        String[] hostList = pingHosts.split(",");
        for (String host : hostList) {
            logger.info("Adding ping service for " + host);
            if (host.length() > 0) {
                // add results for that hostname
                Results.getInstance().addHostname(host);
                // add ping service
                PingService ping = new PingService(host);
                ping.start();
                // add traceroute service
                TracerouteService tr = new TracerouteService(host);
                tr.start();
            }
        }
    }

    private static void addHttpCheckers(Properties properties) {
        String urls = properties.getProperty("httpCheckUrls");
        String[] urlList = urls.split(",");

        String delays = properties.getProperty("httpDelays");
        String[] delayList = delays.split(",");

        String timeouts = properties.getProperty("httpTimeouts");
        String[] timeoutList = timeouts.split(",");

        if ( !( (timeoutList.length == delayList.length) && (delayList.length == urlList.length) ) ) {
            logger.error("URL list, timeout list and delay list must have the same length");
            return;
        }

        for (int i=0; i<urlList.length; i++) {

            String url = urlList[i];
            Integer delay = Integer.valueOf(delayList[i]);
            Integer timeout = Integer.valueOf(timeoutList[i]);

            try {
                URI uri = new URI(url);
                String host = uri.getHost();
                // check if results has the hostname already
                if (!Results.getInstance().hasHostName(host)) {
                    Results.getInstance().addHostname(host);
                }
                HttpCheckService httpCheck = new HttpCheckService(url, delay, timeout);
                httpCheck.start();
            } catch (URISyntaxException e) {
                logger.error("URI syntax exception: " + url);
            }
        }
    }
}
