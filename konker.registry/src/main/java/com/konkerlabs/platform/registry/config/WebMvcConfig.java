package com.konkerlabs.platform.registry.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.CssLinkResourceTransformer;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

import com.konkerlabs.platform.registry.web.converters.InstantToStringConverter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import nz.net.ultraq.thymeleaf.LayoutDialect;

@Configuration
@EnableWebMvc
@ComponentScan(lazyInit = true, basePackages = { "com.konkerlabs.platform.registry.web",
        "com.konkerlabs.platform.registry.integration.endpoints" })
public class WebMvcConfig extends WebMvcConfigurerAdapter implements ApplicationContextAware {

    private static final Config webConfig = ConfigFactory.load().getConfig("web");

    private ApplicationContext applicationContext;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/")
                .setCacheControl(CacheControl.maxAge(72, TimeUnit.HOURS).cachePublic())
                .resourceChain(true)
                .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**")).addTransformer(new CssLinkResourceTransformer());
    }
    
    @Bean
    public ITemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setPrefix("views/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setOrder(3);
        templateResolver.setCheckExistence(true);
        templateResolver.setCacheable(webConfig.getBoolean("views.cache"));
        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setEnableSpringELCompiler(true);
        engine.addDialect(new LayoutDialect());
        engine.addDialect(java8TimeDialect());
        engine.setTemplateResolver(templateResolver());
        return engine;
    }

    @Bean
    public ViewResolver viewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setOrder(1);
        resolver.setCache(true);
        resolver.setCharacterEncoding("UTF-8");
        return resolver;
    }

    @Bean
    public Java8TimeDialect java8TimeDialect() {
        return new Java8TimeDialect();
    }

    @Bean(name = "messageSource")
    public MessageSource getMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.addBasenames("/WEB-INF/i18n/global", "/WEB-INF/i18n/menu", "/WEB-INF/i18n/devices",
                "/WEB-INF/i18n/enrichment", "/WEB-INF/i18n/routes", "/WEB-INF/i18n/destinations",
                "/WEB-INF/i18n/transformations", "/WEB-INF/i18n/integration", "/WEB-INF/i18n/visualization");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(instantToStringConverter());
    }

    @Bean
    public InstantToStringConverter instantToStringConverter() {
        return new InstantToStringConverter();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
