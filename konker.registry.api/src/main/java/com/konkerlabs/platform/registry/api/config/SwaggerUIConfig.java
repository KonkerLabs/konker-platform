package com.konkerlabs.platform.registry.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.google.common.collect.Ordering;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.collect.Lists.newArrayList;

@EnableWebMvc
@Configuration
@EnableSwagger2
public class SwaggerUIConfig extends WebMvcConfigurerAdapter {

    public static final String securitySchemaOAuth2 = "oauth2schema";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo())
                .securitySchemes(newArrayList(securitySchema()))
                .securityContexts(newArrayList(securityContext()))
                // .operationOrdering(getOperationOrdering()) try with swagger 2.7.0
                .tags(new Tag("devices", "Operations to list and edit devices"),
                        new Tag("device credentials", "Operations to manage device credentials (username, password and URLs)"),
                        new Tag("routes", "Operations to list and edit routes"),
                        new Tag("events", "Operations to list incoming and outgoing device events"),
                        new Tag("rest transformations", "Operations to manage Rest Transformations"))
                .enableUrlTemplating(true);

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
                newArrayList(),
                newArrayList(cliGrantType));
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .forPaths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Konker Platform Api")
                .description(
                        "Before access endpoints please " +
                                "<a href='/v1/oauth/token?grant_type=client_credentials' target='_blank'>login</a>")
                .termsOfServiceUrl("https://demo.konkerlabs.net/registry/resources/konker/pdf/termos_de_uso_20161014a-9d089e3f67c4b4ab9c83c0a0313158ef.pdf")
                .contact(new Contact("Konker", "developers.konkerlabs.com", "support@konkerlabs.com"))
                .license("Apache 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0")
                .version("v1")
                .build();
    }


}
