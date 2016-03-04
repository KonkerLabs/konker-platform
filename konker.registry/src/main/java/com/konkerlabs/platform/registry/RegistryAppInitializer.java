package com.konkerlabs.platform.registry;

import com.konkerlabs.platform.registry.config.*;
import com.konkerlabs.platform.utilities.config.UtilitiesConfig;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class RegistryAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] {
                SecurityConfig.class,
                BusinessConfig.class,
                MongoConfig.class,
                MqttConfig.class,
                SmsConfig.class,
                SolrConfig.class,
                UtilitiesConfig.class
        };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { WebMvcConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }
}
