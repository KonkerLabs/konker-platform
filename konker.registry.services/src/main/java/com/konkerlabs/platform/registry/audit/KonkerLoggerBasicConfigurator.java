package com.konkerlabs.platform.registry.audit;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusManager;
import org.slf4j.LoggerFactory;

public class KonkerLoggerBasicConfigurator {

    static final KonkerLoggerBasicConfigurator hiddenSingleton =
            new KonkerLoggerBasicConfigurator();

    private KonkerLoggerBasicConfigurator() {
    }

    public static void configure(KonkerLoggerContext lc) {
        StatusManager sm = lc.getStatusManager();
        if(sm != null) {
            sm.add(new InfoStatus("Setting up default configuration.", lc));
        }

        ConsoleAppender ca = new ConsoleAppender();
        ca.setContext(lc);
        ca.setName("console");
        PatternLayoutEncoder pl = new PatternLayoutEncoder();
        pl.setContext(lc);
        pl.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        pl.start();
        ca.setEncoder(pl);
        ca.start();
        KonkerLogger rootLogger = lc.getLogger("ROOT");
        rootLogger.addAppender(ca);
    }

    public static void configureDefaultContext() {
        KonkerLoggerContext lc = (KonkerLoggerContext) KonkerLoggerFactory.getILoggerFactory();
        configure(lc);
    }
}
