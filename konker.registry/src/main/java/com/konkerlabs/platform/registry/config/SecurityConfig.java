package com.konkerlabs.platform.registry.config;

import com.konkerlabs.platform.registry.security.MongoUserDetailsService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = MongoUserDetailsService.class)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Config securityConfig = ConfigFactory.load().getConfig("security");

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(new PlaintextPasswordEncoder());
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .headers()
                .frameOptions()
                .sameOrigin()
            .and()
                .authorizeRequests()
                    .antMatchers("/resources/**").permitAll()
                    .anyRequest().authenticated()
            .and()
                .formLogin().loginPage(securityConfig.getString("loginPage"))
                            .defaultSuccessUrl(securityConfig.getString("successLoginUrl"))
                            .permitAll()
            .and()
                .logout().logoutSuccessUrl(securityConfig.getString("loginPage"))
            .and()
                .csrf().disable();
    }
}
