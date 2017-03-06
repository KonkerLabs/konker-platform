package com.konkerlabs.platform.registry.audit;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.util.OptionHelper;

public class KonkerStatusListenerConfigHelper {
    public KonkerStatusListenerConfigHelper() {}

    static void installIfAsked(KonkerLoggerContext loggerContext) {
        String slClass = OptionHelper.getSystemProperty("logback.statusListenerClass");
        if(!OptionHelper.isEmpty(slClass)) {
            addStatusListener(loggerContext, slClass);
        }

    }

    private static void addStatusListener(KonkerLoggerContext loggerContext, String listenerClass) {
        Object listener = null;
        if("SYSOUT".equalsIgnoreCase(listenerClass)) {
            listener = new OnConsoleStatusListener();
        } else {
            listener = createListenerPerClassName(loggerContext, listenerClass);
        }

        initAndAddListener(loggerContext, (StatusListener)listener);
    }

    private static void initAndAddListener(KonkerLoggerContext loggerContext, StatusListener listener) {
        if(listener != null) {
            if(listener instanceof ContextAware) {
                ((ContextAware)listener).setContext(loggerContext);
            }

            if(listener instanceof LifeCycle) {
                ((LifeCycle)listener).start();
            }

            loggerContext.getStatusManager().add(listener);
        }

    }

    private static StatusListener createListenerPerClassName(KonkerLoggerContext loggerContext,
                                                             String listenerClass) {
        try {
            return (StatusListener)OptionHelper.instantiateByClassName(listenerClass, StatusListener.class, loggerContext);
        } catch (Exception var3) {
            var3.printStackTrace();
            return null;
        }
    }
}
