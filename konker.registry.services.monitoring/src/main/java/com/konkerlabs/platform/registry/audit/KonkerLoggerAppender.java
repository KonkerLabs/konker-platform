package com.konkerlabs.platform.registry.audit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.konkerlabs.platform.registry.audit.repositories.TenantLogRepository;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.sql.Date;
import java.time.Instant;


public class KonkerLoggerAppender extends AppenderBase<ILoggingEvent> {

    public static final String CONTEXT = "context";

    private TenantLogRepository repository;

    public KonkerLoggerAppender(TenantLogRepository repository) {
        this.repository = repository;
    }

    public KonkerLoggerAppender() {
    }


    @Override
    public void start() {
        super.start();
        if (repository == null) {
            try {
                repository = TenantLogRepository.getInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void doAppend(ILoggingEvent eventObject) {
        super.doAppend(eventObject);
    }

    @Override
    public void append(ILoggingEvent iLoggingEvent) {
        enrich(iLoggingEvent);
    }


    /**
     * Enrich log with tenant info
     *
     * @param eventObject
     */
    private void enrich(ILoggingEvent eventObject) {

        if (eventObject != null && eventObject instanceof LoggingEvent) {
            if (eventObject.getArgumentArray() != null
                    && eventObject.getArgumentArray().length > 1) {
                URI uri = null;
                LogLevel logLevel = null;
                for (Object item : eventObject.getArgumentArray()) {
                    if (item instanceof URI && !((URI) item).getScheme().equals("http")) {
                        uri = (URI) item;
                    }
                    if (item instanceof LogLevel) {
                        logLevel = (LogLevel) item;
                    }
                }
                if (uri != null
                        && logLevel != null
                        && !StringUtils.isEmpty(logLevel.getId())
                        && eventObject.getLevel() != null) {
                    Level userLogLevel = Level.toLevel(logLevel.getId());
                    Level eventLogLevel = eventObject.getLevel();
                    if (eventLogLevel.isGreaterOrEqual(userLogLevel)) {
                        MDC.put(CONTEXT, encodeDealer(uri));
                        store(eventObject, getTenant(uri), eventLogLevel.levelStr, eventObject.getFormattedMessage());
                    }
                }
            }

        }
    }

    /**
     * Store log into datastore
     *
     * @param event
     * @param tenantDomain
     * @param trace
     */
    public void store(ILoggingEvent event, String tenantDomain, String level, String trace) {
        repository.insert(tenantDomain, Date.from(Instant.ofEpochMilli(event.getTimeStamp())), level, trace);
    }

    /**
     * Encode URIDealer to log format
     *
     * @param dealer
     * @return String  - entity: {val}, tentant: {val}, guid: {val}
     */
    private String encodeDealer(URI dealer) {
        return "Entity: " + dealer.getScheme() + ", Tenant: "
                + dealer.getHost() + ", Guid: " + dealer.getPath().replaceAll("/", "");
    }

    /**
     * Return tentant info
     *
     * @param dealer
     * @return tentantDomain
     */
    private String getTenant(URI dealer) {
        return dealer.getHost();
    }

    public void setRepository(TenantLogRepository repository) {
        this.repository = repository;
    }

}
