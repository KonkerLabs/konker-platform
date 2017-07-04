package com.konkerlabs.platform.registry.audit;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggerComparator;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.classic.util.LoggerNameUtil;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;
import org.slf4j.Marker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KonkerLoggerContext extends ContextBase
        implements IKonkerLoggerFactory, LifeCycle {
    final KonkerLogger root = new KonkerLogger("ROOT", (KonkerLogger)null, this);
    private int size;
    private int noAppenderWarning = 0;
    private final List<KonkerLoggerContextListener> loggerContextListenerList = new ArrayList();
    private Map<String, KonkerLogger> loggerCache = new ConcurrentHashMap();
    private KonkerLoggerContextVO loggerContextRemoteView = new KonkerLoggerContextVO(this);
    private final KonkerTurboFilterList turboFilterList = new KonkerTurboFilterList();
    private boolean packagingDataEnabled = true;
    private int maxCallerDataDepth = 8;
    int resetCount = 0;
    private List<String> frameworkPackages;

    public KonkerLoggerContext() {
        this.root.setLevel(Level.DEBUG);
        this.loggerCache.put("ROOT", this.root);
        this.initEvaluatorMap();
        this.size = 1;
        this.frameworkPackages = new ArrayList();
    }

    void initEvaluatorMap() {
        this.putObject("EVALUATOR_MAP", new HashMap());
    }

    private void updateLoggerContextVO() {
        this.loggerContextRemoteView = new KonkerLoggerContextVO(this);
    }

    public void putProperty(String key, String val) {
        super.putProperty(key, val);
        this.updateLoggerContextVO();
    }

    public void setName(String name) {
        super.setName(name);
        this.updateLoggerContextVO();
    }

    public final KonkerLogger getLogger(Class clazz) {
        return this.getLogger(clazz.getName());
    }

    public final KonkerLogger getLogger(String name) {
        if(name == null) {
            throw new IllegalArgumentException("name argument cannot be null");
        } else if("ROOT".equalsIgnoreCase(name)) {
            return this.root;
        } else {
            int i = 0;
            KonkerLogger logger = this.root;
            KonkerLogger childLogger = this.loggerCache.get(name);
            if(childLogger != null) {
                return childLogger;
            } else {
                int h;
                do {
                    h = LoggerNameUtil.getSeparatorIndexOf(name, i);
                    String childName;
                    if(h == -1) {
                        childName = name;
                    } else {
                        childName = name.substring(0, h);
                    }

                    i = h + 1;
                    synchronized(logger) {
                        childLogger = logger.getChildByName(childName);
                        if(childLogger == null) {
                            childLogger = logger.createChildByName(childName);
                            this.loggerCache.put(childName, childLogger);
                            this.incSize();
                        }
                    }

                    logger = childLogger;
                } while(h != -1);

                return childLogger;
            }
        }
    }

    private void incSize() {
        ++this.size;
    }

    int size() {
        return this.size;
    }

    public KonkerLogger exists(String name) {
        return this.loggerCache.get(name);
    }

    final void noAppenderDefinedWarning(KonkerLogger logger) {
        if(this.noAppenderWarning++ == 0) {
            this.getStatusManager().add(new WarnStatus("No appenders present in context [" + this.getName() + "] for logger [" + logger.getName() + "].", logger));
        }

    }

    public List<Logger> getLoggerList() {
        Collection collection = this.loggerCache.values();
        ArrayList loggerList = new ArrayList(collection);
        Collections.sort(loggerList, new LoggerComparator());
        return loggerList;
    }

    public KonkerLoggerContextVO getLoggerContextRemoteView() {
        return this.loggerContextRemoteView;
    }

    public void setPackagingDataEnabled(boolean packagingDataEnabled) {
        this.packagingDataEnabled = packagingDataEnabled;
    }

    public boolean isPackagingDataEnabled() {
        return this.packagingDataEnabled;
    }

    public void reset() {
        ++this.resetCount;
        super.reset();
        this.initEvaluatorMap();
        this.root.recursiveReset();
        this.resetTurboFilterList();
        this.fireOnReset();
        this.resetListenersExceptResetResistant();
        this.resetStatusListeners();
    }

    private void resetStatusListeners() {
        StatusManager sm = this.getStatusManager();
        Iterator i$ = sm.getCopyOfStatusListenerList().iterator();

        while(i$.hasNext()) {
            StatusListener sl = (StatusListener)i$.next();
            sm.remove(sl);
        }

    }

    public KonkerTurboFilterList getTurboFilterList() {
        return this.turboFilterList;
    }

    public void addTurboFilter(KonkerTurboFilter newFilter) {
        this.turboFilterList.add(newFilter);
    }

    public void resetTurboFilterList() {
        Iterator i$ = this.turboFilterList.iterator();

        while(i$.hasNext()) {
            TurboFilter tf = (TurboFilter)i$.next();
            tf.stop();
        }

        this.turboFilterList.clear();
    }

    final FilterReply getTurboFilterChainDecision_0_3OrMore(Marker marker,
                                                            KonkerLogger logger,
                                                            Level level,
                                                            String format,
                                                            Object[] params,
                                                            Throwable t) {
        return this.turboFilterList.size() == 0 ? FilterReply.NEUTRAL :
                this.turboFilterList
                        .getTurboFilterChainDecision(marker, logger, level, format, params, t);
    }

    final FilterReply getTurboFilterChainDecision_1(Marker marker,
                                                    KonkerLogger logger,
                                                    Level level,
                                                    String format,
                                                    Object param,
                                                    Throwable t) {
        return this.turboFilterList.size() == 0 ? FilterReply.NEUTRAL :
                this.turboFilterList.getTurboFilterChainDecision(marker, logger, level, format, new Object[]{param}, t);
    }

    final FilterReply getTurboFilterChainDecision_2(Marker marker,
                                                    KonkerLogger logger,
                                                    Level level,
                                                    String format,
                                                    Object param1,
                                                    Object param2,
                                                    Throwable t) {
        return this.turboFilterList.size() == 0 ? FilterReply.NEUTRAL :
                this.turboFilterList.getTurboFilterChainDecision(marker, logger, level, format, new Object[]{param1, param2}, t);
    }



    public void addListener(KonkerLoggerContextListener listener) {
        this.loggerContextListenerList.add(listener);
    }

    public void removeListener(KonkerLoggerContextListener listener) {
        this.loggerContextListenerList.remove(listener);
    }

    private void resetListenersExceptResetResistant() {
        ArrayList toRetain = new ArrayList();
        Iterator i$ = this.loggerContextListenerList.iterator();

        while(i$.hasNext()) {
            LoggerContextListener lcl = (LoggerContextListener)i$.next();
            if(lcl.isResetResistant()) {
                toRetain.add(lcl);
            }
        }

        this.loggerContextListenerList.retainAll(toRetain);
    }

    private void resetAllListeners() {
        this.loggerContextListenerList.clear();
    }

    public List<LoggerContextListener> getCopyOfListenerList() {
        return new ArrayList(this.loggerContextListenerList);
    }

    void fireOnLevelChange(KonkerLogger logger, Level level) {
        Iterator i$ = this.loggerContextListenerList.iterator();

        while(i$.hasNext()) {
            KonkerLoggerContextListener listener = (KonkerLoggerContextListener) i$.next();
            listener.onLevelChange(logger, level);
        }

    }

    private void fireOnReset() {
        Iterator i$ = this.loggerContextListenerList.iterator();

        while(i$.hasNext()) {
            KonkerLoggerContextListener listener = (KonkerLoggerContextListener) i$.next();
            listener.onReset(this);
        }

    }

    private void fireOnStart() {
        Iterator i$ = this.loggerContextListenerList.iterator();

        while(i$.hasNext()) {
            KonkerLoggerContextListener listener = (KonkerLoggerContextListener) i$.next();
            listener.onStart(this);
        }

    }

    private void fireOnStop() {
        Iterator i$ = this.loggerContextListenerList.iterator();

        while(i$.hasNext()) {
            KonkerLoggerContextListener listener = (KonkerLoggerContextListener) i$.next();
            listener.onStop(this);
        }

    }

    public void start() {
        super.start();
        this.fireOnStart();
    }

    public void stop() {
        this.reset();
        this.fireOnStop();
        this.resetAllListeners();
        super.stop();
    }

    public String toString() {
        return this.getClass().getName() + "[" + this.getName() + "]";
    }

    public int getMaxCallerDataDepth() {
        return this.maxCallerDataDepth;
    }

    public void setMaxCallerDataDepth(int maxCallerDataDepth) {
        this.maxCallerDataDepth = maxCallerDataDepth;
    }

    public List<String> getFrameworkPackages() {
        return this.frameworkPackages;
    }
}