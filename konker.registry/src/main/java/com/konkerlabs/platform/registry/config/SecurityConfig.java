package com.konkerlabs.platform.registry.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Config securityConfig = ConfigFactory.load().getConfig("security");

    @Autowired
    public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .inMemoryAuthentication()
            .withUser(securityConfig.getString("username"))
            .password(securityConfig.getString("password")).roles("ADMIN");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
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
