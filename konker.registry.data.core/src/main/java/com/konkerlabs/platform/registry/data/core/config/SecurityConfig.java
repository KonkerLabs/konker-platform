package com.konkerlabs.platform.registry.data.core.config;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.Filter;
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
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.data.core.security.KonkerBasicAuthenticationFilter;
import com.konkerlabs.platform.registry.data.core.security.KonkerParamsAuthenticationFilter;
import com.konkerlabs.platform.security.managers.PasswordManager;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@EnableWebSecurity
@Order(org.springframework.boot.autoconfigure.security.SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    @SuppressWarnings("serial")
    private static final Map<String, String> CORS_HEADERS = new HashMap<String, String>() {{
        put("Access-Control-Allow-Origin", "{Origin}");
        put("Access-Control-Allow-Methods", "GET,POST");
        put("Access-Control-Allow-Credentials", "true");
        put("Access-Control-Allow-Headers", "Authorization,Content-Type");
    }};

    @Autowired
    @Qualifier("deviceDetails")
    private UserDetailsService detailsService;

    @Autowired
    private DeviceRegisterService deviceRegisterService;

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

        KonkerBasicAuthenticationFilter filter = new KonkerBasicAuthenticationFilter(authenticationManager());
        filter.setDeviceRegisterService(deviceRegisterService);

        Filter paramsAuthFilter = new KonkerParamsAuthenticationFilter(authenticationManager());

        http.csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS)
                    .permitAll()
                .and()
                    .addFilter(filter)
                    .addFilterAfter(paramsAuthFilter, BasicAuthenticationFilter.class)
                    .requestMatchers()
                    .antMatchers("/pub/**", "/sub/**", "/cfg/**", "/upload/**", "/firmware/**")
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
                        } else if ("Access-Control-Allow-Methods".equals(item.getKey()) &&
                        		httpServletRequest.getServletPath().startsWith("/pub/")) {
                        	httpServletResponse.addHeader(
                        			item.getKey(),
                                    "POST");
                        } else if ("Access-Control-Allow-Methods".equals(item.getKey()) &&
                        		httpServletRequest.getServletPath().startsWith("/sub/")) {
                        	httpServletResponse.addHeader(
                        			item.getKey(),
                                    "GET");
                        } else if ("Access-Control-Allow-Methods".equals(item.getKey()) &&
                                httpServletRequest.getServletPath().startsWith("/upload/")) {
                            httpServletResponse.addHeader(
                                    item.getKey(),
                                    "POST");
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
