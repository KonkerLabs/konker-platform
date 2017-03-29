package com.konkerlabs.platform.registry.audit;


import ch.qos.logback.classic.Level;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;
import java.util.concurrent.CopyOnWriteArrayList;

public class KonkerTurboFilterList
        extends CopyOnWriteArrayList<KonkerTurboFilter> {

    public KonkerTurboFilterList() {}

    public FilterReply getTurboFilterChainDecision(Marker marker,
                                                   KonkerLogger logger,
                                                   Level level,
                                                   String format,
                                                   Object[] params,
                                                   Throwable t) {
        int size = this.size();
        if(size == 1) {
            try {
                KonkerTurboFilter filter = this.get(0);
                return filter.decide(marker, logger, level, format, params, t);
            } catch (IndexOutOfBoundsException var13) {
                return FilterReply.NEUTRAL;
            }
        } else {
            Object[] tfa = this.toArray();
            int len = tfa.length;

            for(int i = 0; i < len; ++i) {
                KonkerTurboFilter tf = (KonkerTurboFilter) tfa[i];
                FilterReply r = tf.decide(marker, logger, level, format, params, t);
                if(r == FilterReply.DENY || r == FilterReply.ACCEPT) {
                    return r;
                }
            }

            return FilterReply.NEUTRAL;
        }
    }

}
