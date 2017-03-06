package com.konkerlabs.platform.registry.audit;

import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;
import org.slf4j.helpers.SubstituteLogger;
import org.slf4j.helpers.Util;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public final class KonkerLoggerFactory implements IKonkerLoggerFactory {

    static final String CODES_PREFIX = "http://www.slf4j.org/codes.html";
    static final String NO_STATICLOGGERBINDER_URL = "http://www.slf4j.org/codes.html#StaticLoggerBinder";
    static final String MULTIPLE_BINDINGS_URL = "http://www.slf4j.org/codes.html#multiple_bindings";
    static final String NULL_LF_URL = "http://www.slf4j.org/codes.html#null_LF";
    static final String VERSION_MISMATCH = "http://www.slf4j.org/codes.html#version_mismatch";
    static final String SUBSTITUTE_LOGGER_URL = "http://www.slf4j.org/codes.html#substituteLogger";
    static final String LOGGER_NAME_MISMATCH_URL = "http://www.slf4j.org/codes.html#loggerNameMismatch";
    static final String UNSUCCESSFUL_INIT_URL = "http://www.slf4j.org/codes.html#unsuccessfulInit";
    static final String UNSUCCESSFUL_INIT_MSG = "org.slf4j.LoggerFactory could not be successfully initialized. See also http://www.slf4j.org/codes.html#unsuccessfulInit";
    static final int UNINITIALIZED = 0;
    static final int ONGOING_INITIALIZATION = 1;
    static final int FAILED_INITIALIZATION = 2;
    static final int SUCCESSFUL_INITIALIZATION = 3;
    static final int NOP_FALLBACK_INITIALIZATION = 4;
    static int INITIALIZATION_STATE = 0;
    static KonkerSubstituteLoggerFactory TEMP_FACTORY = new KonkerSubstituteLoggerFactory();
    static NOPLoggerFactory NOP_FALLBACK_FACTORY = new NOPLoggerFactory();
    static final String DETECT_LOGGER_NAME_MISMATCH_PROPERTY = "slf4j.detectLoggerNameMismatch";
    static final String JAVA_VENDOR_PROPERTY = "java.vendor.url";
    static boolean DETECT_LOGGER_NAME_MISMATCH = Util.safeGetBooleanSystemProperty("slf4j.detectLoggerNameMismatch");
    private static final String[] API_COMPATIBILITY_LIST = new String[]{"1.6", "1.7"};
    private static String STATIC_LOGGER_BINDER_PATH = "org/slf4j/impl/StaticLoggerBinder.class";

    private KonkerLoggerFactory() {
    }

    static void reset() {
        INITIALIZATION_STATE = 0;
        TEMP_FACTORY = new KonkerSubstituteLoggerFactory();
    }

    private static final void performInitialization() {
        bind();
        if(INITIALIZATION_STATE == 3) {
            versionSanityCheck();
        }

    }

    private static boolean messageContainsOrgSlf4jImplStaticLoggerBinder(String msg) {
        return msg == null?false:(msg.contains("org/slf4j/impl/StaticLoggerBinder")?true:msg.contains("org.slf4j.impl.StaticLoggerBinder"));
    }

    private static final void bind() {
        String msg;
        try {
            Set e = findPossibleStaticLoggerBinderPathSet();
            reportMultipleBindingAmbiguity(e);
            StaticLoggerBinder.getSingleton();
            INITIALIZATION_STATE = 3;
            reportActualBinding(e);
            fixSubstitutedLoggers();
        } catch (NoClassDefFoundError var2) {
            msg = var2.getMessage();
            if(!messageContainsOrgSlf4jImplStaticLoggerBinder(msg)) {
                failedBinding(var2);
                throw var2;
            }

            INITIALIZATION_STATE = 4;
            Util.report("Failed to load class \"org.slf4j.impl.StaticLoggerBinder\".");
            Util.report("Defaulting to no-operation (NOP) logger implementation");
            Util.report("See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.");
        } catch (NoSuchMethodError var3) {
            msg = var3.getMessage();
            if(msg != null && msg.contains("org.slf4j.impl.StaticLoggerBinder.getSingleton()")) {
                INITIALIZATION_STATE = 2;
                Util.report("slf4j-api 1.6.x (or later) is incompatible with this binding.");
                Util.report("Your binding is version 1.5.5 or earlier.");
                Util.report("Upgrade your binding to version 1.6.x.");
            }

            throw var3;
        } catch (Exception var4) {
            failedBinding(var4);
            throw new IllegalStateException("Unexpected initialization failure", var4);
        }

    }

    static void failedBinding(Throwable t) {
        INITIALIZATION_STATE = 2;
        Util.report("Failed to instantiate SLF4J LoggerFactory", t);
    }

    private static final void fixSubstitutedLoggers() {
        List loggers = TEMP_FACTORY.getLoggers();
        if(!loggers.isEmpty()) {
            Util.report("The following set of substitute loggers may have been accessed");
            Util.report("during the initialization phase. Logging calls during this");
            Util.report("phase were not honored. However, subsequent logging calls to these");
            Util.report("loggers will work as normally expected.");
            Util.report("See also http://www.slf4j.org/codes.html#substituteLogger");
            Iterator i$ = loggers.iterator();

            while(i$.hasNext()) {
                SubstituteLogger subLogger = (SubstituteLogger)i$.next();
                subLogger.setDelegate(getKonkerLogger(subLogger.getName()));
                Util.report(subLogger.getName());
            }

            TEMP_FACTORY.clear();
        }
    }

    private static final void versionSanityCheck() {
        try {
            String e = StaticLoggerBinder.REQUESTED_API_VERSION;
            boolean match = false;
            String[] arr$ = API_COMPATIBILITY_LIST;
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                String aAPI_COMPATIBILITY_LIST = arr$[i$];
                if(e.startsWith(aAPI_COMPATIBILITY_LIST)) {
                    match = true;
                }
            }

            if(!match) {
                Util.report("The requested version " + e + " by your slf4j binding is not compatible with " + Arrays.asList(API_COMPATIBILITY_LIST).toString());
                Util.report("See http://www.slf4j.org/codes.html#version_mismatch for further details.");
            }
        } catch (NoSuchFieldError var6) {
            ;
        } catch (Throwable var7) {
            Util.report("Unexpected problem occured during version sanity check", var7);
        }

    }

    static Set<URL> findPossibleStaticLoggerBinderPathSet() {
        LinkedHashSet staticLoggerBinderPathSet = new LinkedHashSet();

        try {
            ClassLoader ioe = LoggerFactory.class.getClassLoader();
            Enumeration paths;
            if(ioe == null) {
                paths = ClassLoader.getSystemResources(STATIC_LOGGER_BINDER_PATH);
            } else {
                paths = ioe.getResources(STATIC_LOGGER_BINDER_PATH);
            }

            while(paths.hasMoreElements()) {
                URL path = (URL)paths.nextElement();
                staticLoggerBinderPathSet.add(path);
            }
        } catch (IOException var4) {
            Util.report("Error getting resources from path", var4);
        }

        return staticLoggerBinderPathSet;
    }

    private static boolean isAmbiguousStaticLoggerBinderPathSet(Set<URL> staticLoggerBinderPathSet) {
        return staticLoggerBinderPathSet.size() > 1;
    }

    private static void reportMultipleBindingAmbiguity(Set<URL> staticLoggerBinderPathSet) {
        if(!isAndroid()) {
            if(isAmbiguousStaticLoggerBinderPathSet(staticLoggerBinderPathSet)) {
                Util.report("Class path contains multiple SLF4J bindings.");
                Iterator i$ = staticLoggerBinderPathSet.iterator();

                while(i$.hasNext()) {
                    URL path = (URL)i$.next();
                    Util.report("Found binding in [" + path + "]");
                }

                Util.report("See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.");
            }

        }
    }

    private static boolean isAndroid() {
        String vendor = Util.safeGetSystemProperty("java.vendor.url");
        return vendor == null?false:vendor.toLowerCase().contains("android");
    }

    private static void reportActualBinding(Set<URL> staticLoggerBinderPathSet) {
        if(isAmbiguousStaticLoggerBinderPathSet(staticLoggerBinderPathSet)) {
            Util.report("Actual binding is of type [" + StaticLoggerBinder.getSingleton().getLoggerFactoryClassStr() + "]");
        }

    }

    public static KonkerLogger getKonkerLogger(String name) {
        IKonkerLoggerFactory iLoggerFactory = getILoggerFactory();
        return iLoggerFactory.getLogger(name);
    }

    public static KonkerLogger getLogger(Class<?> clazz) {
        KonkerLogger logger = getKonkerLogger(clazz.getName());
        if(DETECT_LOGGER_NAME_MISMATCH) {
            Class autoComputedCallingClass = Util.getCallingClass();
            if(autoComputedCallingClass != null && nonMatchingClasses(clazz, autoComputedCallingClass)) {
                Util.report(String.format("Detected logger name mismatch. Given name: \"%s\"; computed name: \"%s\".", new Object[]{logger.getName(), autoComputedCallingClass.getName()}));
                Util.report("See http://www.slf4j.org/codes.html#loggerNameMismatch for an explanation");
            }
        }

        return logger;
    }

    private static boolean nonMatchingClasses(Class<?> clazz, Class<?> autoComputedCallingClass) {
        return !autoComputedCallingClass.isAssignableFrom(clazz);
    }

    public static IKonkerLoggerFactory getILoggerFactory() {
        if(INITIALIZATION_STATE == 0) {
            synchronized(KonkerLoggerFactory.class) {
                if(INITIALIZATION_STATE == 0) {
                    INITIALIZATION_STATE = 1;
                    performInitialization();
                }
            }
        }

        switch(INITIALIZATION_STATE) {
            case 1:
                return TEMP_FACTORY;
            case 2:
                throw new IllegalStateException("org.slf4j.LoggerFactory could not be successfully initialized. See also http://www.slf4j.org/codes.html#unsuccessfulInit");
            case 3:
                return KonkerStaticLoggerBinder.getSingleton().getLoggerFactory();
            case 4:
                return null; //implement NOP
            default:
                throw new IllegalStateException("Unreachable code");
        }
    }

    @Override
    public KonkerLogger getLogger(String val) {
        return getKonkerLogger(val);
    }
}
