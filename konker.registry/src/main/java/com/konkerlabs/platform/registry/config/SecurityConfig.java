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
import org.springframework.context.annotation.ComponentScan;
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

import com.konkerlabs.platform.registry.security.TenantUserDetailsService;
import com.konkerlabs.platform.security.managers.PasswordManager;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = TenantUserDetailsService.class)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    private static final Config securityConfig = ConfigFactory.load().getConfig("security");

    private static final Map<String, String> CORS_HEADERS = new HashMap<String, String>() {{
        put("Access-Control-Allow-Origin", "{Origin}");
        put("Access-Control-Allow-Methods", "GET,POST");
        put("Access-Control-Allow-Credentials", "true");
        put("Access-Control-Allow-Headers", "Authorization");
    }};


    @Configuration
    @Order(1)
    public static class ApiWebSecurityConfig extends WebSecurityConfigurerAdapter {

        @Autowired
        @Qualifier("deviceDetails")
        private UserDetailsService detailsService;

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
                    .authorizeRequests().antMatchers(HttpMethod.OPTIONS).permitAll()
                    .and().requestMatchers()
                    .antMatchers("/pub/**", "/sub/**").and().authorizeRequests()
                    .anyRequest().hasAuthority("DEVICE").and().httpBasic()
                    .and().headers()
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
                            } else {
                                httpServletResponse.addHeader(
                                        item.getKey(),
                                        item.getValue());
                            }
                        });
                    });

            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        }
    }

    @Configuration
    @Order(2)
    public static class FormWebSecurityConfig extends WebSecurityConfigurerAdapter {

        @Autowired
        @Qualifier("tenantUserDetails")
        private UserDetailsService userDetailsService;

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
            authenticationProvider.setUserDetailsService(userDetailsService);
            authenticationProvider.setPasswordEncoder(new PlaintextPasswordEncoder());
            auth.authenticationProvider(authenticationProvider);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.headers().frameOptions().sameOrigin().and().authorizeRequests().antMatchers("/resources/**")
                    .permitAll().anyRequest().hasAuthority("USER").and().formLogin()
                    .loginPage(securityConfig.getString("loginPage"))
                    .defaultSuccessUrl(securityConfig.getString("successLoginUrl")).permitAll().and().logout()
                    .logoutSuccessUrl(securityConfig.getString("loginPage")).and().csrf().disable();
        }
    }
    
    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
    	return super.authenticationManager();
    }
}
