package com.konkerlabs.platform.registry;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.konkerlabs.platform.registry.config.BusinessConfig;
import com.konkerlabs.platform.registry.config.IntegrationConfig;
import com.konkerlabs.platform.registry.config.MongoConfig;
import com.konkerlabs.platform.registry.config.RedisConfig;
import com.konkerlabs.platform.registry.config.SecurityConfig;
import com.konkerlabs.platform.registry.config.SolrConfig;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.utilities.config.UtilitiesConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;

public class RegistryAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	private static final Config smsConfig = ConfigFactory.load().getConfig("sms");
    private static final Config analyticsConfig = ConfigFactory.load().getConfig("konkerAnalytics");
	private static final Logger LOGGER = LoggerFactory.getLogger(RegistryAppInitializer.class);

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class<?>[] { SecurityConfig.class, BusinessConfig.class, MongoConfig.class, IntegrationConfig.class,
				SolrConfig.class, UtilitiesConfig.class, RedisConfig.class };
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
		return new Filter[] { new HiddenHttpMethodFilter(), new ResourceUrlEncodingFilter() };
	}

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {

		super.onStartup(servletContext);
		servletContext.addListener(new RequestContextListener());

		// verifying configs for features activation
        Set<String> profiles = new HashSet<String>();
		if (isSmsFeaturesEnabled()) {
            profiles.add("sms");
		}
		if (isKonkerAnalyticsEnabled()) {
            profiles.add("konkerAnalytics");
        }
        servletContext.setInitParameter("spring.profiles.active", StringUtils.arrayToCommaDelimitedString(profiles.toArray()));
	}

	private boolean isSmsFeaturesEnabled() {
		boolean isEnabled = false;

		try {
			isEnabled = Optional.ofNullable(smsConfig.getBoolean("enabled")).orElse(false);
		} catch (ConfigException e) {
			LOGGER.error(
					"SMS configuration has no values for key 'enabled'. SMS features are being thoroughly disabled on the platform.",
					e);
		}
		return isEnabled;

	}

    private boolean isKonkerAnalyticsEnabled() {
        boolean isEnabled = false;

        try {
            isEnabled = Optional.ofNullable(analyticsConfig.getBoolean("enabled")).orElse(false);
        } catch (ConfigException e) {
            LOGGER.error(
                    "Konker Analytics configuration has no values for key 'enabled'. Platform menu is being built for IoT features.",
                    e);
        }
        return isEnabled;

    }

}
