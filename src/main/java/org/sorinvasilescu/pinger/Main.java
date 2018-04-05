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
import org.sorinvasilescu.pinger.service.HttpService;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

public class Main {

    private static Logger logger = LogManager.getLogger();

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
        HttpService httpService = new HttpService(host,port);
        httpService.run();
    }
}
