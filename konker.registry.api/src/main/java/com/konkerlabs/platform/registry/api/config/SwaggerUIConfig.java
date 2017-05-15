package com.konkerlabs.platform.registry.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

@EnableWebMvc
@Configuration
@EnableSwagger2
public class SwaggerUIConfig extends WebMvcConfigurerAdapter {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public static final String securitySchemaOAuth2 = "oauth2schema";
    public static final String authorizationScopeRead = "read";
    public static final String authorizationScopeGlobalDesc = "Access IoT Resources";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("default")
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.any())
                .build()
                .protocols(Sets.newHashSet("https"))
                .apiInfo(apiInfo())
                .securitySchemes(newArrayList(securitySchema()))
                .securityContexts(newArrayList(securityContext()))
                // .operationOrdering(getOperationOrdering()) try with swagger 2.7.0
                .tags(new Tag("devices", "Operations to list and edit devices"),
                        new Tag("device credentials", "Operations to manage device credentials (username, password and URLs)"),
                        new Tag("device models", "Operations to manage device models"),
                        new Tag("routes", "Operations to list and edit routes"),
                        new Tag("users", "Operations to list and edit organization users"),
                        new Tag("events", "Operations to query incoming and outgoing device events"),
                        new Tag("applications", "Operations to list organization applications"),
                        new Tag("rest destinations", "Operations to list organization REST destinations"),
                        new Tag("rest transformations", "Operations to manage REST transformations"),
                        new Tag("locations", "Operations to list and edit locations"),
                        new Tag("device configs", "Operations to list and edit device configurations"))
                .enableUrlTemplating(false);

    }

    @SuppressWarnings("unused")
    private Ordering<Operation> getOperationOrdering() {
        return new Ordering<Operation>() {
            @Override
            public int compare(Operation left, Operation right) {
                int result = left.getMethod().compareTo(right.getMethod());
                if (result != 0) return result;

                return 0;
            }
        };
    }

    @SuppressWarnings("unused")
    private BasicAuth basicSecuritySchema() {
        return new BasicAuth("basic");
    }

    private OAuth securitySchema() {

        ClientCredentialsGrant cliGrantType =
                new ClientCredentialsGrant("/v1/oauth/token");

        return new OAuth(
                securitySchemaOAuth2,
                newArrayList(
                ),
                newArrayList(cliGrantType));
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.any())
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = new AuthorizationScope(authorizationScopeRead, authorizationScopeGlobalDesc);
        return newArrayList(
                new SecurityReference(securitySchemaOAuth2, authorizationScopes));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .version("v1")
                .build();
    }

    @SuppressWarnings("unused")
    private String getDescription(String filename) {

        Map<String, Object> defaultMap = new HashMap<>();
        defaultMap.put("swagger.hostname", "localhost:8080");
        Config defaultConf = ConfigFactory.parseMap(defaultMap);

        Config config = ConfigFactory.load().withFallback(defaultConf);
        String hostname = config.getString("swagger.hostname");

        try {
            InputStream is = new ClassPathResource("description.md").getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String         line = null;
            StringBuilder  stringBuilder = new StringBuilder();
            String         ls = System.getProperty("line.separator");

            try {
                while((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(ls);
                }

                return stringBuilder.toString().replace("<HOSTNAME>", hostname);
            } finally {
                reader.close();
            }

        } catch (IOException e) {
            LOGGER.error("Error getting description.html content...", e);
            return "";
        }

    }

}
