package com.konkerlabs.platform.registry.audit;

import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.EnvUtil;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

public class KonkerContextInitializer {
    public static final String GROOVY_AUTOCONFIG_FILE = "logback.groovy";
    public static final String AUTOCONFIG_FILE = "logback.xml";
    public static final String TEST_AUTOCONFIG_FILE = "logback-test.xml";
    public static final String CONFIG_FILE_PROPERTY = "logback.configurationFile";
    public static final String STATUS_LISTENER_CLASS = "logback.statusListenerClass";
    public static final String SYSOUT = "SYSOUT";
    final KonkerLoggerContext loggerContext;

    public KonkerContextInitializer(KonkerLoggerContext loggerContext) {
        this.loggerContext = loggerContext;
    }

    public void configureByResource(URL url) throws JoranException {
        if (url == null) {
            throw new IllegalArgumentException("URL argument cannot be null");
        } else {
            String urlString = url.toString();
            if (!urlString.endsWith("xml")) {
                throw new LogbackException("Unexpected filename extension of file [" + url.toString() + "]. Should be either .groovy or .xml");
            }
            JoranConfigurator configurator1 = new JoranConfigurator();
            configurator1.setContext(this.loggerContext);
            configurator1.doConfigure(url);


        }
    }

    void joranConfigureByResource(URL url) throws JoranException {
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(this.loggerContext);
        configurator.doConfigure(url);
    }

    private URL findConfigFileURLFromSystemProperties(ClassLoader classLoader, boolean updateStatus) {
        String logbackConfigFile = OptionHelper.getSystemProperty("logback.configurationFile");
        if (logbackConfigFile != null) {
            URL result = null;

            URL f;
            try {
                result = new URL(logbackConfigFile);
                URL e = result;
                return e;
            } catch (MalformedURLException var13) {
                result = Loader.getResource(logbackConfigFile, classLoader);
                if (result == null) {
                    File f1 = new File(logbackConfigFile);
                    if (!f1.exists() || !f1.isFile()) {
                        return null;
                    }

                    try {
                        result = f1.toURI().toURL();
                        URL e1 = result;
                        return e1;
                    } catch (MalformedURLException var12) {
                        return null;
                    }
                }

                f = result;
            } finally {
                if (updateStatus) {
                    this.statusOnResourceSearch(logbackConfigFile, classLoader, result);
                }

            }

            return f;
        } else {
            return null;
        }
    }

    public URL findURLOfDefaultConfigurationFile(boolean updateStatus) {
        ClassLoader myClassLoader = Loader.getClassLoaderOfObject(this);
        URL url = this.findConfigFileURLFromSystemProperties(myClassLoader, updateStatus);
        if (url != null) {
            return url;
        } else {
            url = this.getResource("logback.groovy", myClassLoader, updateStatus);
            if (url != null) {
                return url;
            } else {
                url = this.getResource("logback-test.xml", myClassLoader, updateStatus);
                return url != null ? url : this.getResource("logback.xml", myClassLoader, updateStatus);
            }
        }
    }

    private URL getResource(String filename, ClassLoader myClassLoader, boolean updateStatus) {
        URL url = Loader.getResource(filename, myClassLoader);
        if (updateStatus) {
            this.statusOnResourceSearch(filename, myClassLoader, url);
        }

        return url;
    }

    public void autoConfig() throws JoranException {
        KonkerStatusListenerConfigHelper.installIfAsked(this.loggerContext);
        URL url = this.findURLOfDefaultConfigurationFile(true);
        if (url != null) {
            this.configureByResource(url);
        } else {
            KonkerLoggerConfigurator c = (KonkerLoggerConfigurator)
                    EnvUtil.loadFromServiceLoader(KonkerLoggerConfigurator.class);
            if (c != null) {
                try {
                    c.setContext(this.loggerContext);
                    c.configure(this.loggerContext);
                } catch (Exception var4) {
                    throw new LogbackException(String.format("Failed to initialize Configurator: %s using ServiceLoader", new Object[]{c != null ? c.getClass().getCanonicalName() : "null"}), var4);
                }
            } else {
                KonkerLoggerBasicConfigurator.configure(this.loggerContext);
            }
        }

    }

    private void multiplicityWarning(String resourceName, ClassLoader classLoader) {
        Set urlSet = null;
        StatusManager sm = this.loggerContext.getStatusManager();

        try {
            urlSet = Loader.getResourceOccurrenceCount(resourceName, classLoader);
        } catch (IOException var7) {
            sm.add(new ErrorStatus("Failed to get url list for resource [" + resourceName + "]", this.loggerContext, var7));
        }

        if (urlSet != null && urlSet.size() > 1) {
            sm.add(new WarnStatus("Resource [" + resourceName + "] occurs multiple times on the classpath.", this.loggerContext));
            Iterator i$ = urlSet.iterator();

            while (i$.hasNext()) {
                URL url = (URL) i$.next();
                sm.add(new WarnStatus("Resource [" + resourceName + "] occurs at [" + url.toString() + "]", this.loggerContext));
            }
        }

    }

    private void statusOnResourceSearch(String resourceName, ClassLoader classLoader, URL url) {
        StatusManager sm = this.loggerContext.getStatusManager();
        if (url == null) {
            sm.add(new InfoStatus("Could NOT find resource [" + resourceName + "]", this.loggerContext));
        } else {
            sm.add(new InfoStatus("Found resource [" + resourceName + "] at [" + url.toString() + "]", this.loggerContext));
            this.multiplicityWarning(resourceName, classLoader);
        }

    }
}
