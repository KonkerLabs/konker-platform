package com.konkerlabs.platform.registry.audit;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class KonkerLoggerAsyncAppender extends AsyncAppender {

    @Override
    public void start() {
        super.start();
    }

    @Override
    protected boolean isDiscardable(ILoggingEvent event) {
        return false;
    }
}
