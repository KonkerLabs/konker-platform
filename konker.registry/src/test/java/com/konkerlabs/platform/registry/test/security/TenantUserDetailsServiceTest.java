package com.konkerlabs.platform.registry.test.security;

import com.konkerlabs.platform.registry.security.TenantUserDetailsService;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.base.RedisTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        TenantUserDetailsServiceTest.SecurityConfig.class,
        RedisTestConfiguration.class
})
public class TenantUserDetailsServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private UserDetailsService userDetailsService;
    private String userEmail = "admin@konkerlabs.com";

    @Test
    @UsingDataSet(locations = "/fixtures/users.json")
    public void shouldLoadUserByItsUsername() throws Exception {
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
        assertThat(userDetails,notNullValue());
    }

    @Test
    public void shouldRaiseAnExceptionIfUsernameDoesNotExist() throws Exception {
        thrown.expect(UsernameNotFoundException.class);
        thrown.expectMessage("authentication.credentials.invalid");

        userDetailsService.loadUserByUsername(userEmail);
    }

    @Configuration
    static class SecurityConfig {

        @Bean
        public UserDetailsService userDetailsService() {
            return new TenantUserDetailsService();
        }
    }

}