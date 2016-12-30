package com.konkerlabs.platform.registry.test.web.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import com.konkerlabs.platform.registry.web.converters.utils.ConverterUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Event.EventActor;
import com.konkerlabs.platform.registry.business.model.EventSchema;
import com.konkerlabs.platform.registry.business.model.EventSchema.SchemaField;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Token;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EmailService;
import com.konkerlabs.platform.registry.business.services.api.EventSchemaService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TokenService;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.security.UserContextResolver;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import org.springframework.web.servlet.LocaleResolver;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        WebMvcConfig.class,
        WebTestConfiguration.class,
        SecurityTestConfiguration.class,
        RecoverPasswordControllerTest.RecoverTestContextConfig.class
})
public class RecoverPasswordControllerTest extends WebLayerTestContext {

    private static final String USER_EMAIL = "user@testdomain.com";
    private static final String USER_EMAIL_INVALID = "common@testdomain.com";

    @Autowired
    ApplicationContext applicationContext;
    
    @Autowired
    EmailService emailService;
    
    @Autowired
    TokenService tokenService;
    
    @Autowired
    UserService userService;
    
    @Autowired
    private User user;

    private Token token;
    
    @Before
    public void setUp() {
    	token = Token.builder()
    				.creationDateTime(Instant.now())
    				.isExpired(false)
    				.purpose(TokenService.Purpose.RESET_PASSWORD.name())
    				.token("8a4fd7bd-503e-4e4a-b85e-5501305c7a98")
    				.userEmail("user@testdomain.com")
    				.build();
    }

    @After
    public void tearDown() {

    }
    
    @Test
    public void shouldReturnFalseIfEmailInvalid() throws Exception {
    	when(userService.findByEmail(USER_EMAIL_INVALID))
    		.thenReturn(ServiceResponseBuilder.<User>error()
    		.withResult(null).build());
    	
    	getMockMvc().perform(get("/recoverpassword").param("email", USER_EMAIL_INVALID))
    		.andDo(print())
    		.andExpect(content().string("false"));
    }
    
    @Test
    public void shouldReturnTrueIfUserEmailValid() throws Exception {
    	when(userService.findByEmail(USER_EMAIL))
    		.thenReturn(ServiceResponseBuilder.<User>ok()
    		.withResult(user).build());
    	
    	when(tokenService.generateToken(TokenService.Purpose.RESET_PASSWORD, user, Duration.ofMinutes(60)))
    		.thenReturn(ServiceResponseBuilder.<String>ok()
    		.withResult(token.getToken()).build());
    	
    	getMockMvc().perform(get("/recoverpassword").param("email", USER_EMAIL))
    		.andDo(print())
    		.andExpect(content().string("true"));
    }
    
    @Configuration
    static class RecoverTestContextConfig {
    	@Bean
        public EmailService emailService() {
            return Mockito.mock(EmailService.class);
        }
        @Bean
        public TokenService tokenService() { 
        	return Mockito.mock(TokenService.class); 
        }
        
        @Bean
        public UserService userSer() {
        	return Mockito.mock(UserService.class);
        }
    }
}