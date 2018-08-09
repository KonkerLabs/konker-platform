package com.konkerlabs.platform.registry.audit;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.*;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.MDCAdapter;

import java.util.HashMap;
import java.util.Map;

public class KonkerLoggingEvent
        extends LoggingEvent
        implements ILoggingEvent {

    transient String fqnOfLoggerClass;
    private String threadName;
    private String loggerName;
    private KonkerLoggerContext loggerContext;
    private KonkerLoggerContextVO loggerContextVO;
    private transient Level level;
    private String message;
    transient String formattedMessage;
    private transient Object[] argumentArray;
    private ThrowableProxy throwableProxy;
    private StackTraceElement[] callerDataArray;
    private Marker marker;
    private Map<String, String> mdcPropertyMap;
    private static final Map<String, String> CACHED_NULL_MAP = new HashMap();
    private long timeStamp;

    public KonkerLoggingEvent() {
    }

    public KonkerLoggingEvent(String fqcn, KonkerLogger logger,
                              Level level, String message, Throwable throwable, Object[] argArray) {
        this.fqnOfLoggerClass = fqcn;
        this.loggerName = logger.getName();
        this.loggerContext = logger.getLoggerContext();
        this.loggerContextVO = this.loggerContext.getLoggerContextRemoteView();
        this.level = level;
        this.message = message;
        this.argumentArray = argArray;
        if(throwable == null) {
            throwable = this.extractThrowableAnRearrangeArguments(argArray);
        }

        if(throwable != null) {
            this.throwableProxy = new ThrowableProxy(throwable);
            KonkerLoggerContext lc = logger.getLoggerContext();
            if(lc.isPackagingDataEnabled()) {
                this.throwableProxy.calculatePackagingData();
            }
        }

        this.timeStamp = System.currentTimeMillis();
    }

    private Throwable extractThrowableAnRearrangeArguments(Object[] argArray) {
        Throwable extractedThrowable = EventArgUtil.extractThrowable(argArray);
        if(EventArgUtil.successfulExtraction(extractedThrowable)) {
            this.argumentArray = EventArgUtil.trimmedCopy(argArray);
        }

        return extractedThrowable;
    }

    public void setArgumentArray(Object[] argArray) {
        if(this.argumentArray != null) {
            throw new IllegalStateException("argArray has been already set");
        } else {
            this.argumentArray = argArray;
        }
    }

    public Object[] getArgumentArray() {
        return this.argumentArray;
    }

    public Level getLevel() {
        return this.level;
    }

    public String getLoggerName() {
        return this.loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getThreadName() {
        if(this.threadName == null) {
            this.threadName = Thread.currentThread().getName();
        }

        return this.threadName;
    }

    public void setThreadName(String threadName) throws IllegalStateException {
        if(this.threadName != null) {
            throw new IllegalStateException("threadName has been already set");
        } else {
            this.threadName = threadName;
        }
    }

    public IThrowableProxy getThrowableProxy() {
        return this.throwableProxy;
    }

    public void setThrowableProxy(ThrowableProxy tp) {
        if(this.throwableProxy != null) {
            throw new IllegalStateException("ThrowableProxy has been already set.");
        } else {
            this.throwableProxy = tp;
        }
    }

    public void prepareForDeferredProcessing() {
        this.getFormattedMessage();
        this.getThreadName();
        this.getMDCPropertyMap();
    }

    public KonkerLoggerContextVO getKonkerLoggerContextVO() {
        return this.loggerContextVO;
    }

    public void setLoggerContextRemoteView(KonkerLoggerContextVO loggerContextVO) {
        this.loggerContextVO = loggerContextVO;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        if(this.message != null) {
            throw new IllegalStateException("The message for this event has been set already.");
        } else {
            this.message = message;
        }
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setLevel(Level level) {
        if(this.level != null) {
            throw new IllegalStateException("The level has been already set for this event.");
        } else {
            this.level = level;
        }
    }

    public StackTraceElement[] getCallerData() {
        if(this.callerDataArray == null) {
            this.callerDataArray = CallerData.extract(new Throwable(), this.fqnOfLoggerClass, this.loggerContext.getMaxCallerDataDepth(), this.loggerContext.getFrameworkPackages());
        }

        return this.callerDataArray;
    }

    public boolean hasCallerData() {
        return this.callerDataArray != null;
    }

    public void setCallerData(StackTraceElement[] callerDataArray) {
        this.callerDataArray = callerDataArray;
    }

    public Marker getMarker() {
        return this.marker;
    }

    public void setMarker(Marker marker) {
        if(this.marker != null) {
            throw new IllegalStateException("The marker has been already set for this event.");
        } else {
            this.marker = marker;
        }
    }

    public long getContextBirthTime() {
        return this.loggerContextVO.getBirthTime();
    }

    public String getFormattedMessage() {
        if(this.formattedMessage != null) {
            return this.formattedMessage;
        } else {
            if(this.argumentArray != null) {
                this.formattedMessage = MessageFormatter.arrayFormat(this.message, this.argumentArray).getMessage();
            } else {
                this.formattedMessage = this.message;
            }

            return this.formattedMessage;
        }
    }

    public Map<String, String> getMDCPropertyMap() {
        if(this.mdcPropertyMap == null) {
            MDCAdapter mdc = MDC.getMDCAdapter();
            if(mdc instanceof LogbackMDCAdapter) {
                this.mdcPropertyMap = ((LogbackMDCAdapter)mdc).getPropertyMap();
            } else {
                this.mdcPropertyMap = mdc.getCopyOfContextMap();
            }
        }

        if(this.mdcPropertyMap == null) {
            this.mdcPropertyMap = CACHED_NULL_MAP;
        }

        return this.mdcPropertyMap;
    }

    public void setMDCPropertyMap(Map<String, String> map) {
        if(this.mdcPropertyMap != null) {
            throw new IllegalStateException("The MDCPropertyMap has been already set for this event.");
        } else {
            this.mdcPropertyMap = map;
        }
    }
}
