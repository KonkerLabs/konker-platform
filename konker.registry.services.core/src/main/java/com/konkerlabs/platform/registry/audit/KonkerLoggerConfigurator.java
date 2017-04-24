package com.konkerlabs.platform.registry.audit;

import ch.qos.logback.core.spi.ContextAware;

public interface KonkerLoggerConfigurator  extends ContextAware {

    void configure(KonkerLoggerContext context);

}
