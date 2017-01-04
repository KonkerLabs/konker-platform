package com.konkerlabs.platform.registry.audit;

import ch.qos.logback.classic.selector.ContextSelector;

import java.util.Arrays;
import java.util.List;

public class KonkerDefaultContextSelector {
    private KonkerLoggerContext defaultLoggerContext;

    public KonkerDefaultContextSelector(KonkerLoggerContext context) {
        this.defaultLoggerContext = context;
    }

    public KonkerLoggerContext getLoggerContext() {
        return this.getDefaultLoggerContext();
    }

    public KonkerLoggerContext getDefaultLoggerContext() {
        return this.defaultLoggerContext;
    }

    public KonkerLoggerContext detachLoggerContext(String loggerContextName) {
        return this.defaultLoggerContext;
    }

    public List<String> getContextNames() {
        return Arrays.asList(new String[]{this.defaultLoggerContext.getName()});
    }

    public KonkerLoggerContext getLoggerContext(String name) {
        return this.defaultLoggerContext.getName().equals(name)?this.defaultLoggerContext:null;
    }
}
