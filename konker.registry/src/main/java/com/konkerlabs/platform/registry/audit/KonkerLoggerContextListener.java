package com.konkerlabs.platform.registry.audit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggerContextListener;

public interface KonkerLoggerContextListener extends LoggerContextListener {

    void onLevelChange(KonkerLogger var1, Level var2);

    void onStart(KonkerLoggerContext var1);

    void onReset(KonkerLoggerContext var1);

    void onStop(KonkerLoggerContext var1);
}
