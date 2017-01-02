package com.konkerlabs.platform.registry.test.web.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.time.Duration;
import java.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.konkerlabs.platform.registry.business.model.Token;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.EmailService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TokenService;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;

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
    
    private static final String JSON = "{\"email\" : \"user@testdomain.com\"}";
    private static final String JSON_INVALID_USER = "{\"email\" : \"common@testdomain.com\"}";

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
    private Token invalidToken;
    
    @Before
    public void setUp() {
    	token = Token.builder()
    				.creationDateTime(Instant.now())
    				.isExpired(false)
    				.purpose(TokenService.Purpose.RESET_PASSWORD.name())
    				.token("8a4fd7bd-503e-4e4a-b85e-5501305c7a98")
    				.userEmail("user@testdomain.com")
    				.build();
    	
    	invalidToken = Token.builder()
				.creationDateTime(Instant.now())
				.isExpired(true)
				.purpose(TokenService.Purpose.RESET_PASSWORD.name())
				.token("8a4fd7bd-503e-4e4a-b85e-5501305c7a99")
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
    	
    	getMockMvc().perform(post("/recoverpassword")
    			.contentType(MediaType.APPLICATION_JSON)
    			.content(JSON_INVALID_USER))
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
    	
    	getMockMvc().perform(post("/recoverpassword")
    			.contentType(MediaType.APPLICATION_JSON)
    			.content(JSON))
    		.andDo(print())
    		.andExpect(content().string("true"));
    }
    
//    @Test
    public void shouldRaiseAnExceptionIfTokenInvalid() throws Exception {
    	when(tokenService.getToken("8a4fd7bd-503e-4e4a-b85e-5501305c7a99"))
			.thenReturn(ServiceResponseBuilder.<Token>ok()
			.withResult(invalidToken).build());
    	
    	getMockMvc().perform(get("/recoverpassword/8a4fd7bd-503e-4e4a-b85e-5501305c7a99"))
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