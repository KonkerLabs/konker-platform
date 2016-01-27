package com.konkerlabs.platform.registry;

import com.konkerlabs.platform.registry.config.BusinessConfig;
import com.konkerlabs.platform.registry.config.MongoConfig;
import com.konkerlabs.platform.registry.config.MqttConfig;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class RegistryAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] {
            BusinessConfig.class,
            MongoConfig.class,
            MqttConfig.class
        };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { WebMvcConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/registry/*" };
    }

}
