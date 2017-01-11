package com.konkerlabs.platform.registry.audit;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContextAware;

import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;


public class KonkerLoggerAppender extends AsyncAppender {

    public static final String CONTEXT = "context";
    private Mongo mongo;



    @Override
    public void start() {
        super.start();
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void preprocess(ILoggingEvent eventObject) {
        super.preprocess(eventObject);
        enrich(eventObject);
    }



    /**
     * Enrich log with tenant info
     * @param eventObject
     */
    private void enrich(ILoggingEvent eventObject) {

        if (eventObject != null && eventObject instanceof LoggingEvent) {
            if (eventObject.getArgumentArray() != null
                    && eventObject.getArgumentArray().length > 1 ){
                URI uri = null;
                LogLevel logLevel = null;
                for(Object item : eventObject.getArgumentArray()){
                    if (item instanceof URI && !((URI) item).getScheme().equals("http")) {
                        uri = (URI) item;
                    }
                    if(item instanceof LogLevel){
                        logLevel = (LogLevel) item;
                    }
                }
                if(uri != null && logLevel != null){
                    MDC.put(CONTEXT, encodeDealer(uri));
                    store(getTenant(uri), eventObject.getFormattedMessage());
                }
            }

        }
    }

    /**
     * Store log into datastore
     * @param tenantDomain
     * @param trace
     */
    private void store(String tenantDomain, String trace) {

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
     * @param dealer
     * @return tentantDomain
     */
    private String getTenant(URI dealer) {
        return dealer.getHost();
    }

    /**
     * Return schema
     * @param dealer
     * @return entityName
     */
    private String getEntity(URI dealer) {
        return dealer.getScheme();
    }
}
