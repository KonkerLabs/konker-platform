package com.konkerlabs.platform.registry;

import com.konkerlabs.platform.registry.config.*;
import com.konkerlabs.platform.utilities.config.UtilitiesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegistryAppInitializer
        extends AbstractAnnotationConfigDispatcherServletInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(RegistryAppInitializer.class);


    @Override
    protected Class<?>[] getRootConfigClasses() {
        try {
            return findMyTypes(
                    "com.konkerlabs.platform.registry.config",
                    "com.konkerlabs.platform.utilities.config");
        } catch (IOException | ClassNotFoundException e) {
            LOG.error("Error scanning packages to boot application configs...", e);
        }
        return null;
    }

    private String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
    }

    private Class[] findMyTypes(String... basePackages) throws IOException, ClassNotFoundException {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
        List<Class> candidates = new ArrayList<>();
        for (String basePackage : basePackages) {
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    resolveBasePackage(basePackage) + "/" + "**/*.class";
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    candidates.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
                }
            }

        }
        Class[] configResources = new Class[candidates.size()];
        int iterator = 0;
        for(Class clazz : candidates){
            configResources[iterator] = clazz;
            iterator++;
        }
        return configResources;
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
        if (isSmsFeaturesEnabled()) {
            profiles.add("sms");
        }
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

    private boolean isSmsFeaturesEnabled() {
        SmsConfig config = new SmsConfig();
        return config.isEnabled();
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
