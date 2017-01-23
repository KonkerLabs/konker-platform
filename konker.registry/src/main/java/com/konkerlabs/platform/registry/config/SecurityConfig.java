package com.konkerlabs.platform.registry.config;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

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
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.konkerlabs.platform.registry.security.TenantUserDetailsService;
import com.konkerlabs.platform.security.managers.PasswordManager;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Getter;
import lombok.Setter;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = TenantUserDetailsService.class)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);
    private static final int MIN_DELAY_TIME = 100;
    private static final int MAX_DELAY_TIME = 250;

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
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public static class FormWebSecurityConfig extends WebSecurityConfigurerAdapter {

        @Autowired
        @Qualifier("tenantUserDetails")
        private UserDetailsService userDetailsService;
        
        @Setter
        @Getter
        private String loginPage;
        
        @Setter
        @Getter
        private String successLoginUrl;
        
        public FormWebSecurityConfig() {
        	Map<String, Object> defaultMap = new HashMap<>();
        	defaultMap.put("security.loginPage", "/login");
        	defaultMap.put("security.successLoginUrl", "/");
        	Config defaultConf = ConfigFactory.parseMap(defaultMap);

        	Config config = ConfigFactory.load().withFallback(defaultConf);
        	setLoginPage(config.getString("security.loginPage"));
        	setSuccessLoginUrl(config.getString("security.successLoginUrl"));
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
            authenticationProvider.setUserDetailsService(userDetailsService);
            authenticationProvider.setPasswordEncoder(new PlaintextPasswordEncoder() {
                @Override
                public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
                    try {
                        Random random = new Random();
                        Boolean result = new PasswordManager().validatePassword(rawPass, encPass);
                        // Delay time introduced to prevent user enumeration attack
                        int delayTime = random.nextInt(MAX_DELAY_TIME - MIN_DELAY_TIME) + MIN_DELAY_TIME;
                        Thread.sleep(delayTime);
                        return result;
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException | InterruptedException e) {
                        LOGGER.error(e.getMessage(), e);
                        return false;
                    }
                }
            });
            auth.authenticationProvider(authenticationProvider);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.headers().frameOptions().sameOrigin().and().authorizeRequests()
                    .antMatchers("/resources/**", "/recoverpassword/**")
                    .permitAll().anyRequest().hasAuthority("LOGIN").and().formLogin()
                    .loginPage(getLoginPage())
                    .defaultSuccessUrl(getSuccessLoginUrl()).permitAll().and().logout()
                    .logoutSuccessUrl(getLoginPage()).and().csrf().disable();
        }


        @Bean
        @Override
        protected AuthenticationManager authenticationManager() throws Exception {
        	return super.authenticationManager();
        }
    }
    
}
