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

        if (properties.containsKey("logPath")) {
            addFileLogger(properties.getProperty("logPath"));
        }

        return true;
    }

    private static void addFileLogger(String logPath) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        final LoggerConfig rootConfig = config.getRootLogger();

        Layout<? extends Serializable> layout = PatternLayout.newBuilder()
                            .withPattern("%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n")
                            .build();

        Appender appender = FileAppender.newBuilder()
                .withFileName(logPath)
                .withAppend(true)
                .withLayout(layout)
                .withName("File")
                .build();
        appender.start();

        config.addAppender(appender);
        rootConfig.addAppender(appender, Level.WARN, null);
        ctx.updateLoggers();

        logger.info("Loggers configured");
    }
}
