package com.konkerlabs.platform.registry.audit;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.spi.LifeCycle;
import org.slf4j.Marker;

public abstract class KonkerTurboFilter
        extends ContextAwareBase implements LifeCycle  {

        private String name;
        boolean start = false;

        public KonkerTurboFilter() {}

        public abstract FilterReply decide(Marker var1,
                                           KonkerLogger logger,
                                           Level level,
                                           String var4,
                                           Object[] var5,
                                           Throwable var6);

        public void start() {
            this.start = true;
        }

        public boolean isStarted() {
            return this.start;
        }

        public void stop() {
            this.start = false;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

}
