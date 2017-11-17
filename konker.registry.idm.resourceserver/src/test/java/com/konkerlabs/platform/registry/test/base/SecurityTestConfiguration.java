package com.konkerlabs.platform.registry.test.base;

import com.konkerlabs.platform.registry.business.model.Tenant;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

@Configuration
public class SecurityTestConfiguration {

    @Bean
    public FactoryBean<Tenant> tenantFactoryBean() {
        return new FactoryBean<Tenant>() {
            @Override
            public Tenant getObject() throws Exception {
                return Tenant.builder()
                    .id("tenant_id")
                    .domainName("domainName")
                    .name("Konker").build();
            }

            @Override
            public Class<?> getObjectType() {
                return Tenant.class;
            }

            @Override
            public boolean isSingleton() {
                return false;
            }
        };
    }

}
