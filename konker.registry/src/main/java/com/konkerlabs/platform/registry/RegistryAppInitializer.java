package com.konkerlabs.platform.registry;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.konkerlabs.platform.registry.config.BusinessConfig;
import com.konkerlabs.platform.registry.config.IntegrationConfig;
import com.konkerlabs.platform.registry.config.MongoConfig;
import com.konkerlabs.platform.registry.config.RedisConfig;
import com.konkerlabs.platform.registry.config.SecurityConfig;
import com.konkerlabs.platform.registry.config.SolrConfig;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.utilities.config.UtilitiesConfig;

public class RegistryAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

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
		return new Filter[] { new HiddenHttpMethodFilter() };
	}

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);
		servletContext.addListener(new RequestContextListener());
		servletContext.setInitParameter("spring.profiles.active", "sms");
	}
}
