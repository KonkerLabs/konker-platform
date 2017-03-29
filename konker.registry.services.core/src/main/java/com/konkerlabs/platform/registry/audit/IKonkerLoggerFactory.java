package com.konkerlabs.platform.registry.audit;


import org.slf4j.ILoggerFactory;

public interface IKonkerLoggerFactory extends ILoggerFactory {

    KonkerLogger getLogger(String val);
}
