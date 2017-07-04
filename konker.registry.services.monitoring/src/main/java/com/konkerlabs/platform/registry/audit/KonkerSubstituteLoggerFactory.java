package com.konkerlabs.platform.registry.audit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class KonkerSubstituteLoggerFactory implements IKonkerLoggerFactory {
    final ConcurrentMap<String, KonkerLogger> loggers = new ConcurrentHashMap();

    public KonkerSubstituteLoggerFactory() {
    }

    public KonkerLogger getLogger(String name) {
        KonkerLogger logger = (KonkerLogger) this.loggers.get(name);
        if(logger == null) {
            logger = new KonkerLogger(name);
            KonkerLogger oldLogger = (KonkerLogger) this.loggers.putIfAbsent(name, logger);
            if(oldLogger != null) {
                logger = oldLogger;
            }
        }

        return logger;
    }

    public List<String> getLoggerNames() {
        return new ArrayList(this.loggers.keySet());
    }

    public List<KonkerLogger> getLoggers() {
        return new ArrayList(this.loggers.values());
    }

    public void clear() {
        this.loggers.clear();
    }
}
