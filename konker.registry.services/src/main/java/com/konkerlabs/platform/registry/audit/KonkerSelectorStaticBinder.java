package com.konkerlabs.platform.registry.audit;


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class KonkerSelectorStaticBinder {
    static KonkerSelectorStaticBinder singleton = new KonkerSelectorStaticBinder();
    KonkerDefaultContextSelector contextSelector;
    Object key;

    public KonkerSelectorStaticBinder() {
    }

    public static KonkerSelectorStaticBinder getSingleton() {
        return singleton;
    }

    public void init(KonkerLoggerContext defaultLoggerContext, Object key) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if(this.key == null) {
            this.key = key;
        } else if(this.key != key) {
            throw new IllegalAccessException("Only certain classes can access this method.");
        }

        String contextSelectorStr = OptionHelper.getSystemProperty("logback.ContextSelector");
        if(contextSelectorStr == null) {
            this.contextSelector = new KonkerDefaultContextSelector(defaultLoggerContext);
        } else {
            this.contextSelector = dynamicalContextSelector(defaultLoggerContext, contextSelectorStr);
        }

    }

    static KonkerDefaultContextSelector dynamicalContextSelector(KonkerLoggerContext defaultLoggerContext, String contextSelectorStr) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class contextSelectorClass = Loader.loadClass(contextSelectorStr);
        Constructor cons = contextSelectorClass.getConstructor(new Class[]{LoggerContext.class});
        return (KonkerDefaultContextSelector) cons.newInstance(new Object[]{defaultLoggerContext});
    }

    public KonkerDefaultContextSelector getContextSelector() {
        return this.contextSelector;
    }
}
