package com.konkerlabs.platform.registry;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import com.konkerlabs.platform.registry.config.OAuth2Config;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.konkerlabs.platform.registry.config.CdnConfig;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.MessageSourceConfig;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.config.RabbitMQConfig;
import com.konkerlabs.platform.registry.config.WebMvcConfig;

public class RegistryAppInitializer
        extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] {
            // Warning: ORDER IS IMPORTANT!
            com.konkerlabs.platform.registry.config.SecurityConfig.class,
            com.konkerlabs.platform.registry.config.BusinessConfig.class,
            com.konkerlabs.platform.registry.config.MongoConfig.class,
            com.konkerlabs.platform.registry.config.MongoAuditConfig.class,
            com.konkerlabs.platform.registry.config.MongoBillingConfig.class,
            com.konkerlabs.platform.registry.config.CassandraConfig.class,
            com.konkerlabs.platform.utilities.config.UtilitiesConfig.class,
            com.konkerlabs.platform.registry.config.SpringMailConfig.class,
            com.konkerlabs.platform.registry.config.WebConfig.class,
            com.konkerlabs.platform.registry.config.CdnConfig.class,
            com.konkerlabs.platform.registry.config.RecaptchaConfig.class,
            com.konkerlabs.platform.registry.config.EmailConfig.class,
            com.konkerlabs.platform.registry.config.AmazonConfig.class,
            com.konkerlabs.platform.registry.config.EnvironmentConfig.class,
            com.konkerlabs.platform.registry.config.EventStorageConfig.class,
            com.konkerlabs.platform.registry.config.OAuth2Config.class,
            com.konkerlabs.platform.registry.config.RabbitMQConfig.class,
            com.konkerlabs.platform.registry.config.MessageSourceConfig.class,
            com.konkerlabs.platform.registry.config.RedisCacheConfig.class,
            com.konkerlabs.platform.registry.config.RestTemplateConfig.class

        };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{WebMvcConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    protected Filter[] getServletFilters() {
        return new Filter[]{new HiddenHttpMethodFilter(), new ResourceUrlEncodingFilter()};
    }

    @Override
    protected void customizeRegistration(Dynamic registration) {
        registration.setInitParameter("throwExceptionIfNoHandlerFound", "true");
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        super.onStartup(servletContext);
        servletContext.addListener(new RequestContextListener());

        // verifying configs for features activation
        Set<String> profiles = new HashSet<String>();
        if (isEmailFeaturesEnabled()) {
            profiles.add("email");
        }
        if (isCdnFeaturesEnabled()) {
            profiles.add("cdn");
        }
        if (isSslFeaturesEnabled()) {
            profiles.add("ssl");
        }

        servletContext.setInitParameter("spring.profiles.active", StringUtils.arrayToCommaDelimitedString(profiles.toArray()));
    }

    private boolean isEmailFeaturesEnabled() {
        EmailConfig config = new EmailConfig();
        return config.isEnabled();
    }

    private boolean isCdnFeaturesEnabled() {
        CdnConfig config = new CdnConfig();
        return config.isEnabled();
    }

    private boolean isSslFeaturesEnabled() {
        PubServerConfig config = new PubServerConfig();
        return config.isSslEnabled();
    }

}
