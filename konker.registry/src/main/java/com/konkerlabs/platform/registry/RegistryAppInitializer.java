package com.konkerlabs.platform.registry;

import com.konkerlabs.platform.registry.config.*;
import com.konkerlabs.platform.utilities.config.UtilitiesConfig;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class RegistryAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] {
                SecurityConfig.class,
                BusinessConfig.class,
                MongoConfig.class,
                IntegrationConfig.class,
                SolrConfig.class,
                UtilitiesConfig.class,
                RedisConfig.class
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

    @Override
    protected Filter[] getServletFilters() {
        return new Filter[] { new HiddenHttpMethodFilter() };
    }
    
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
    	super.onStartup(servletContext);
    	servletContext.setInitParameter("spring.profiles.active", "sms");
    }
}
