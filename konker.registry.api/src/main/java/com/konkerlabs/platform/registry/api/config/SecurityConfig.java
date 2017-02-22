package com.konkerlabs.platform.registry.api.config;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.konkerlabs.platform.registry.api.security.TenantUserDetailsService;
import com.konkerlabs.platform.security.managers.PasswordManager;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = {
        "com.konkerlabs.platform.registry.api.security",
    }
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private TenantUserDetailsService tenantUserDetailsService;
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(tenantUserDetailsService);
        authenticationProvider.setPasswordEncoder(new PlaintextPasswordEncoder() {
            @Override
            public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
                try {
                    return new PasswordManager().validatePassword(rawPass, encPass);
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    LOGGER.error(e.getMessage(), e);
                    return false;
                }
            }
        });
        
        auth.authenticationProvider(authenticationProvider);

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().fullyAuthenticated();
        http.httpBasic();
        http.csrf().disable();
    }

}
