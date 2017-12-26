package com.konkerlabs.platform.registry.api.config;

import com.google.common.base.Predicate;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
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
import static com.typesafe.config.ConfigFactory.load;
import static com.typesafe.config.ConfigFactory.parseMap;

@EnableWebMvc
@Configuration
@EnableSwagger2
public class SwaggerUIConfig extends WebMvcConfigurerAdapter {

    private static final String SWAGGER_HOSTNAME = "swagger.hostname";
    private static final String SWAGGER_PROTOCOL = "swagger.protocol";

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
                .protocols(Sets.newHashSet(getSwaggerConfig().getString(SWAGGER_PROTOCOL)))
                .apiInfo(apiInfo())
                .securitySchemes(newArrayList(securitySchema()))
                .securityContexts(newArrayList(securityContext()))
                // .operationOrdering(getOperationOrdering()) try with swagger 2.7.0
                .tags(
                        new Tag("alert triggers", "Operations to manage alert triggers"),
                        new Tag("applications", "Operations to list organization applications"),
                        new Tag("application document store", "Operations to manage generic documents storage"),
                        new Tag("device configs", "Operations to manage device configurations"),
                        new Tag("device credentials", "Operations to manage device credentials (username, password and URLs)"),
                        new Tag("device firmwares", "Operations to manage device firmwares"),
                        new Tag("device models", "Operations to manage device models"),
                        new Tag("device status", "Operations to verify the device status"),
                        new Tag("devices", "Operations to manage devices"),
                        new Tag("devices custom data", "Operations to manage devices custom data"),
                        new Tag("events", "Operations to query incoming and outgoing device events"),
                        new Tag("gateways", "Operations to manage gateways"),
                        new Tag("locations", "Operations to manage locations"),
                        new Tag("rest destinations", "Operations to list organization REST destinations"),
                        new Tag("rest transformations", "Operations to manage REST transformations"),
                        new Tag("routes", "Operations to manage routes"),
                        new Tag("users", "Operations to manage organization users"),
                        new Tag("user subscription", "Operations to subscribe new users")

                     )
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
                .forPaths(regex())
                .build();
    }

    private Predicate<String> regex() {
        return input -> !input.equals("/userSubscription");
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

        Config config = getSwaggerConfig();
        String hostname = config.getString(SWAGGER_HOSTNAME);

        try {
            InputStream is = new ClassPathResource("description.md").getInputStream();
            String         line;
            StringBuilder  stringBuilder = new StringBuilder();
            String         ls = System.getProperty("line.separator");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(ls);
                }

                return stringBuilder.toString().replace("<HOSTNAME>", hostname);
            }

        } catch (IOException e) {
            LOGGER.error("Error getting description.html content...", e);
            return "";
        }

    }

    private Config getSwaggerConfig() {
        Map<String, Object> defaultMap = new HashMap<>();
        defaultMap.put(SWAGGER_HOSTNAME, "localhost:8080");
        defaultMap.put(SWAGGER_PROTOCOL, "http");
        Config defaultConf = parseMap(defaultMap);

        return load().withFallback(defaultConf);
    }

}
