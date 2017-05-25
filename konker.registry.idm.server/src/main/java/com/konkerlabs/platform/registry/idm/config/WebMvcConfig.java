package com.konkerlabs.platform.registry.idm.config;

/*@EnableWebMvc
@Configuration*/
public class WebMvcConfig {//extends WebMvcConfigurerAdapter implements ApplicationContextAware {

/*
    @Autowired
    private WebConfig webConfig;

    private ApplicationContext applicationContext;


    *//*@Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setEnableSpringELCompiler(true);
        engine.addDialect(new LayoutDialect());
        engine.addDialect(java8TimeDialect());
        engine.addDialect(new SpringSecurityDialect());
        engine.addDialect(new KonkerDialect());
        engine.setTemplateResolver(templateResolver());
        return engine;
    }*//*

   *//* @Bean
    public ViewResolver viewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setOrder(1);
        resolver.setCache(true);
        resolver.setCharacterEncoding("UTF-8");
        return resolver;
    }*//*


    @Bean
    public LocaleResolver localeResolver() {
        final SessionLocaleResolver ret = new SessionLocaleResolver();
        return ret;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
        registry.addInterceptor(new UserDefinedLocaleHandlerInterceptor());
        super.addInterceptors(registry);
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        super.configureDefaultServletHandling(configurer);
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor(){
        LocaleChangeInterceptor localeChangeInterceptor= new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
        return localeChangeInterceptor;
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

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("*//**");
    }*/
}
