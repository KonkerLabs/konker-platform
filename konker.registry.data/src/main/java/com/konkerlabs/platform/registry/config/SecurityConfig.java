package com.konkerlabs.platform.registry.config;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.konkerlabs.platform.security.managers.PasswordManager;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    @SuppressWarnings("serial")
    private static final Map<String, String> CORS_HEADERS = new HashMap<String, String>() {{
        put("Access-Control-Allow-Origin", "{Origin}");
        put("Access-Control-Allow-Methods", "GET,POST");
        put("Access-Control-Allow-Credentials", "true");
        put("Access-Control-Allow-Headers", "Authorization,Content-Type");
    }};

    @Configuration
    @Order(1)
    public static class ApiWebSecurityConfig extends WebSecurityConfigurerAdapter {

        @Autowired
        @Qualifier("deviceDetails")
        private UserDetailsService detailsService;

        @Autowired
        @Qualifier("customBasicAuthFilter")
        private BasicAuthenticationFilter filter;

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
            authenticationProvider.setUserDetailsService(detailsService);
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
            http.csrf().disable()
                    .authorizeRequests()
                    .antMatchers(HttpMethod.OPTIONS)
                        .permitAll()
                    .and()
                        .addFilter(filter)
                        .requestMatchers()
                        .antMatchers("/pub/**", "/sub/**")
                    .and()
                        .authorizeRequests()
                        .anyRequest()
                        .hasAuthority("DEVICE")
                    .and()
                        .httpBasic()
                    .and()
                        .headers()
                        .addHeaderWriter((HttpServletRequest httpServletRequest,
                                          HttpServletResponse httpServletResponse) -> {

                        CORS_HEADERS.entrySet().stream().forEach(item -> {
                            if (item.getValue().matches("\\{(.*?)\\}")) {
                                httpServletResponse.addHeader(
                                        item.getKey(),
                                        Optional.ofNullable(httpServletRequest
                                                .getHeader(item.getValue().replaceAll("[\\{\\}]", "")))
                                                .map(origin -> origin)
                                                .orElse("*"));
                            } else if (item.getKey().equals("Access-Control-Allow-Methods") &&
                            		httpServletRequest.getServletPath().startsWith("/pub/")) {
                            	httpServletResponse.addHeader(
                            			item.getKey(),
                                        "POST");
                            } else if (item.getKey().equals("Access-Control-Allow-Methods") &&
                            		httpServletRequest.getServletPath().startsWith("/sub/")) {
                            	httpServletResponse.addHeader(
                            			item.getKey(),
                                        "GET");
                            } else {
                                httpServletResponse.addHeader(
                                        item.getKey(),
                                        item.getValue());
                            }
                        });
                    });

            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        }

        @Bean
        @Override
        protected AuthenticationManager authenticationManager() throws Exception {
        	return super.authenticationManager();
        }

    }

}
