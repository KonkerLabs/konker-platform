package com.konkerlabs.platform.registry.audit;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusUtil;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.helpers.Util;
import org.slf4j.spi.LoggerFactoryBinder;


public class KonkerStaticLoggerBinder implements LoggerFactoryBinder {

    public static String REQUESTED_API_VERSION = "1.6";
    static final String NULL_CS_URL = "http://logback.qos.ch/codes.html#null_CS";
    private static KonkerStaticLoggerBinder SINGLETON = new KonkerStaticLoggerBinder();
    private static Object KEY = new Object();
    private boolean initialized = false;
    private KonkerLoggerContext defaultLoggerContext = new KonkerLoggerContext();
    private final KonkerSelectorStaticBinder contextSelectorBinder =
            KonkerSelectorStaticBinder.getSingleton();

    private KonkerStaticLoggerBinder() {
        this.defaultLoggerContext.setName("default");
    }

    public static KonkerStaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    static void reset() {
        SINGLETON = new KonkerStaticLoggerBinder();
        SINGLETON.init();
    }

    void init() {
        try {
            try {
                (new KonkerContextInitializer(this.defaultLoggerContext)).autoConfig();
            } catch (JoranException var2) {
                Util.report("Failed to auto configure default logger context", var2);
            }

            if(!StatusUtil.contextHasStatusListener(this.defaultLoggerContext)) {
                StatusPrinter.printInCaseOfErrorsOrWarnings(this.defaultLoggerContext);
            }

            this.contextSelectorBinder.init(this.defaultLoggerContext, KEY);
            this.initialized = true;
        } catch (Throwable var3) {
            Util.report("Failed to instantiate [" + LoggerContext.class.getName() + ']', var3);
        }

    }

    public IKonkerLoggerFactory getLoggerFactory() {
        if(!this.initialized) {
            return this.defaultLoggerContext;
        } else if(this.contextSelectorBinder.getContextSelector() == null) {
            throw new IllegalStateException("contextSelector cannot be null. See also http://logback.qos.ch/codes.html#null_CS");
        } else {
            return this.contextSelectorBinder.getContextSelector().getLoggerContext();
        }
    }

    public String getLoggerFactoryClassStr() {
        return this.contextSelectorBinder.getClass().getName();
    }

    static {
        SINGLETON.init();
    }
}
