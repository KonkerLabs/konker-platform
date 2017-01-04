package com.konkerlabs.platform.registry.audit;

import org.slf4j.MDC;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.helpers.Util;
import org.slf4j.impl.StaticMDCBinder;
import org.slf4j.spi.MDCAdapter;

import java.util.Map;

public class KonkerMDCLogger implements MDCAdapter {

    static final String NULL_MDCA_URL = "http://www.slf4j.org/codes.html#null_MDCA";
    static final String NO_STATIC_MDC_BINDER_URL = "http://www.slf4j.org/codes.html#no_static_mdc_binder";
    static MDCAdapter mdcAdapter;


    private KonkerMDCLogger() {
    }

    private static MDCAdapter bwCompatibleGetMDCAdapterFromBinder() throws NoClassDefFoundError {
        return StaticMDCBinder.SINGLETON.getMDCA();
    }

    @Override
    public void put(String key, String val) throws IllegalArgumentException {
        if(key == null) {
            throw new IllegalArgumentException("key parameter cannot be null");
        } else if(mdcAdapter == null) {
            throw new IllegalStateException("MDCAdapter cannot be null. See also http://www.slf4j.org/codes.html#null_MDCA");
        } else {
            mdcAdapter.put(key, val);
        }
    }

    @Override
    public String get(String key) throws IllegalArgumentException {
        if(key == null) {
            throw new IllegalArgumentException("key parameter cannot be null");
        } else if(mdcAdapter == null) {
            throw new IllegalStateException("MDCAdapter cannot be null. See also http://www.slf4j.org/codes.html#null_MDCA");
        } else {
            return mdcAdapter.get(key);
        }
    }

    @Override
    public void remove(String key) throws IllegalArgumentException {
        if(key == null) {
            throw new IllegalArgumentException("key parameter cannot be null");
        } else if(mdcAdapter == null) {
            throw new IllegalStateException("MDCAdapter cannot be null. See also http://www.slf4j.org/codes.html#null_MDCA");
        } else {
            mdcAdapter.remove(key);
        }
    }

    @Override
    public void clear() {
        if(mdcAdapter == null) {
            throw new IllegalStateException("MDCAdapter cannot be null. See also http://www.slf4j.org/codes.html#null_MDCA");
        } else {
            mdcAdapter.clear();
        }
    }

    @Override
    public Map<String, String> getCopyOfContextMap() {
        if(mdcAdapter == null) {
            throw new IllegalStateException("MDCAdapter cannot be null. See also http://www.slf4j.org/codes.html#null_MDCA");
        } else {
            return mdcAdapter.getCopyOfContextMap();
        }
    }

    @Override
    public void setContextMap(Map<String, String> contextMap) {
        if(mdcAdapter == null) {
            throw new IllegalStateException("MDCAdapter cannot be null. See also http://www.slf4j.org/codes.html#null_MDCA");
        } else {
            mdcAdapter.setContextMap(contextMap);
        }
    }

    public static MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    static {
        try {
            mdcAdapter = bwCompatibleGetMDCAdapterFromBinder();
        } catch (NoClassDefFoundError var2) {
            mdcAdapter = new NOPMDCAdapter();
            String msg = var2.getMessage();
            if(msg == null || !msg.contains("StaticMDCBinder")) {
                throw var2;
            }

            Util.report("Failed to load class \"org.slf4j.impl.StaticMDCBinder\".");
            Util.report("Defaulting to no-operation MDCAdapter implementation.");
            Util.report("See http://www.slf4j.org/codes.html#no_static_mdc_binder for further details.");
        } catch (Exception var3) {
            Util.report("MDC binding unsuccessful.", var3);
        }

    }
}
