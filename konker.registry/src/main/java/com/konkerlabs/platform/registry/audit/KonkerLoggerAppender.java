package com.konkerlabs.platform.registry.audit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import com.konkerlabs.platform.registry.business.model.User;
import org.slf4j.MDC;

import java.net.URI;


public class KonkerLoggerAppender extends RollingFileAppender<Object> {

    public static final String CONTEXT = "context";

    @Override
    protected void append(Object eventObject) {
        if(eventObject != null && eventObject instanceof LoggingEvent) {
            if (((LoggingEvent) eventObject).getArgumentArray() != null
                    && ((LoggingEvent) eventObject).getArgumentArray().length > 0
                    && ((LoggingEvent) eventObject).getArgumentArray()[0] instanceof URI) {

                URI dealer = (URI) ((LoggingEvent) eventObject).getArgumentArray()[0];
                MDC.put(CONTEXT, encodeDealer(dealer));
                if(((LoggingEvent) eventObject).getArgumentArray().length > 1
                        && ((LoggingEvent) eventObject).getArgumentArray()[1] instanceof User){

                    User loggedUser = (User) ((LoggingEvent) eventObject)
                            .getArgumentArray()[1];

                    if(loggedUser != null) { //TODO implement user log level
                        super.append(eventObject);
                    } else {
                        //Non logged user debug
                        if(((LoggingEvent) eventObject).getLevel().equals(Level.DEBUG)){
                            super.append(eventObject);
                        }
                    }
                } else {
                    //TODO implement user log level
                    super.append(eventObject);
                }
            }
        } else {
            super.append(eventObject);
        }
    }

    /**
     * Encode URIDealer to log format
     * @param dealer
     * @return String  - entity: {val}, tentant: {val}, guid: {val}
     */
    private String encodeDealer(URI dealer){
        return "Entity: " + dealer.getScheme() + ", Tenant: "
                + dealer.getHost() + ", Guid: " + dealer.getPath().replaceAll("/", "");
    }
}
