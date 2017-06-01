package com.konkerlabs.platform.registry.test.web.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.konkerlabs.platform.registry.business.model.Token;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.web.services.api.CaptchaService;
import com.konkerlabs.platform.registry.business.services.api.EmailService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TokenService;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.business.services.api.UserService.Validations;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.HotjarConfig;
import com.konkerlabs.platform.registry.config.RecaptchaConfig;
import com.konkerlabs.platform.registry.config.WebConfig;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.controllers.RecoverPasswordController;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        WebMvcConfig.class,
        WebTestConfiguration.class,
        SecurityTestConfiguration.class,
        RecoverPasswordControllerTest.RecoverTestContextConfig.class, 
        WebConfig.class,
        RecaptchaConfig.class,
        EmailConfig.class, 
        HotjarConfig.class
})
@ActiveProfiles("email")
public class RecoverPasswordControllerTest extends WebLayerTestContext {

    private static final String USER_EMAIL = "user@testdomain.com";
    private static final String USER_EMAIL_INVALID = "common@testdomain.com";
    
    private static final String JSON = "{\"email\" : \"user@testdomain.com\", \"recaptcha\" : \"abc123456\"}";
    private static final String JSON_INVALID_USER = "{\"email\" : \"common@testdomain.com\", \"recaptcha\" : \"abc123456\"}";

    @Autowired
    ApplicationContext applicationContext;
    
    @Autowired
    EmailService emailService;
    
    @Autowired
    TokenService tokenService;

    @Autowired
    CaptchaService captchaService;

    @Autowired
    UserService userService;
    
    @Autowired
    private User user;

    private Token token;
    private Token invalidToken;
    private MultiValueMap<String, String> userData;
    
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
    	
    	userData = new LinkedMultiValueMap<>();
    	userData.add("email", user.getEmail());
    	userData.add("name", user.getName());
    	userData.add("username", user.getUsername());
    	userData.add("newPassword", "qwertyqwertyqwerty");
    	userData.add("newPasswordConfirmation", "qwertyqwertyqwerty");
    	userData.add("token", "8a4fd7bd-503e-4e4a-b85e-5501305c7a98");
    }

    @After
    public void tearDown() {

    }
    
    @Test
    public void shouldReturnFalseIfEmailInvalid() throws Exception {
    	when(captchaService.validateCaptcha(anyString(), anyString(), anyString()))
        .thenReturn(ServiceResponseBuilder.<Boolean>ok()
                .withResult(Boolean.TRUE).build());

    	when(userService.findByEmail(USER_EMAIL_INVALID))
    		.thenReturn(ServiceResponseBuilder.<User>error()
    		.withResult(null).build());
    	
    	getMockMvc().perform(post("/recoverpassword/email")
    			.contentType(MediaType.APPLICATION_JSON)
    			.content(JSON_INVALID_USER))
    		.andDo(print())
    		.andExpect(content().string("false"));
    }
    
    @Test
    public void shouldReturnTrueIfUserEmailValid() throws Exception {
        when(captchaService.validateCaptcha(anyString(), anyString(), anyString()))
                .thenReturn(ServiceResponseBuilder.<Boolean>ok()
                        .withResult(Boolean.TRUE).build());

        when(userService.findByEmail(USER_EMAIL))
    		.thenReturn(ServiceResponseBuilder.<User>ok()
    		.withResult(user).build());
    	
    	when(tokenService.generateToken(TokenService.Purpose.RESET_PASSWORD, user, Duration.ofMinutes(15)))
    		.thenReturn(ServiceResponseBuilder.<String>ok()
    		.withResult(token.getToken()).build());

        getMockMvc().perform(post("/recoverpassword/email")
    			.contentType(MediaType.APPLICATION_JSON)
    			.content(JSON))
    		.andDo(print())
    		.andExpect(content().string("true"));
    }
    
    @Test
    public void shouldRaiseAnExceptionIfTokenExpired() throws Exception {
    	when(tokenService.getToken("8a4fd7bd-503e-4e4a-b85e-5501305c7a99"))
			.thenReturn(ServiceResponseBuilder.<Token>ok()
			.withResult(invalidToken).build());
    	
    	List<String> errors = new ArrayList<>();
    	errors.add(applicationContext.getMessage(TokenService.Validations.EXPIRED_TOKEN.getCode(), null, Locale.ENGLISH));
    	
    	getMockMvc().perform(get("/recoverpassword/8a4fd7bd-503e-4e4a-b85e-5501305c7a99"))
    		.andDo(print())
			.andExpect(model().attribute("errors", equalTo(errors)))
			.andExpect(model().attribute("isExpired", equalTo(true)));
    }
    
    @Test
    public void shouldRaiseAnExceptionIfTokenInvalid() throws Exception {
    	when(tokenService.getToken("8a4fd7bd-503e-4e4a-b85e-5501305c7a99"))
			.thenReturn(ServiceResponseBuilder.<Token>error()
			.withMessage(TokenService.Validations.INVALID_TOKEN.getCode())		
			.withResult(null).build());
    	
    	List<String> errors = new ArrayList<>();
    	errors.add(applicationContext.getMessage(TokenService.Validations.INVALID_TOKEN.getCode(), null, Locale.ENGLISH));
    	
    	getMockMvc().perform(get("/recoverpassword/8a4fd7bd-503e-4e4a-b85e-5501305c7a99"))
    		.andDo(print())
			.andExpect(model().attribute("errors", equalTo(errors)))
			.andExpect(model().attribute("isExpired", equalTo(true)));
    }
    
    @Test
    public void shouldRaiseAnExceptionIfTokenInvalidForTime() throws Exception {
    	when(tokenService.getToken("8a4fd7bd-503e-4e4a-b85e-5501305c7a98"))
			.thenReturn(ServiceResponseBuilder.<Token>ok()
			.withResult(token).build());
		
		when(tokenService.isValidToken("8a4fd7bd-503e-4e4a-b85e-5501305c7a98"))
			.thenReturn(ServiceResponseBuilder.<Boolean>ok()
			.withResult(false).build());
    	
    	List<String> errors = new ArrayList<>();
    	errors.add(applicationContext.getMessage(TokenService.Validations.EXPIRED_TOKEN.getCode(), null, Locale.ENGLISH));
    	
    	getMockMvc().perform(get("/recoverpassword/8a4fd7bd-503e-4e4a-b85e-5501305c7a98"))
    		.andDo(print())
			.andExpect(model().attribute("errors", equalTo(errors)))
			.andExpect(model().attribute("isExpired", equalTo(true)));
    }
    
    @Test
    public void shouldShowResetPage() throws Exception {
    	when(tokenService.getToken("8a4fd7bd-503e-4e4a-b85e-5501305c7a98"))
			.thenReturn(ServiceResponseBuilder.<Token>ok()
			.withResult(token).build());
    	
    	when(tokenService.isValidToken("8a4fd7bd-503e-4e4a-b85e-5501305c7a98"))
			.thenReturn(ServiceResponseBuilder.<Boolean>ok()
			.withResult(true).build());
    	
    	when(userService.findByEmail("user@testdomain.com"))
    		.thenReturn(ServiceResponseBuilder.<User>ok()
    		.withResult(user).build());
    	
    	
    	getMockMvc().perform(get("/recoverpassword/8a4fd7bd-503e-4e4a-b85e-5501305c7a98"))
    		.andDo(print())
			.andExpect(model().attribute("user", equalTo(user)));
    }
    
    @Test
    public void shouldRaiseAnExceptionIfUserNotExists() throws Exception {
    	when(tokenService.getToken("8a4fd7bd-503e-4e4a-b85e-5501305c7a98"))
			.thenReturn(ServiceResponseBuilder.<Token>ok()
			.withResult(token).build());
    	
    	when(tokenService.isValidToken("8a4fd7bd-503e-4e4a-b85e-5501305c7a98"))
			.thenReturn(ServiceResponseBuilder.<Boolean>ok()
			.withResult(true).build());
    	
    	when(userService.findByEmail(token.getUserEmail()))
    		.thenReturn(ServiceResponseBuilder.<User>error()
    		.withMessage(Validations.NO_EXIST_USER.getCode()).build());
    	
    	List<String> errors = new ArrayList<>();
    	errors.add(applicationContext.getMessage(RecoverPasswordController.Messages.USER_DOES_NOT_EXIST.getCode(), null, Locale.ENGLISH));
    	
		getMockMvc().perform(post("/recoverpassword").params(userData))
    		.andDo(print())
			.andExpect(model().attribute("errors", equalTo(errors)));
    }
    
    @Test
    public void shouldRaiseAnExceptionIfTokenInvalidInReset() throws Exception {
    	when(tokenService.getToken("8a4fd7bd-503e-4e4a-b85e-5501305c7a98"))
			.thenReturn(ServiceResponseBuilder.<Token>error()
			.withMessage(TokenService.Validations.INVALID_TOKEN.getCode())		
			.withResult(null).build());
    	
    	List<String> errors = new ArrayList<>();
    	errors.add(applicationContext.getMessage(TokenService.Validations.INVALID_TOKEN.getCode(), null, Locale.ENGLISH));
    	
    	getMockMvc().perform(post("/recoverpassword").params(userData))
    		.andDo(print())
			.andExpect(model().attribute("errors", equalTo(errors)))
			.andExpect(model().attribute("isExpired", equalTo(true)));
    }
    
    @Test
    public void shouldRaiseAnExceptionIfTokenExpiredInReset() throws Exception {
    	when(tokenService.getToken("8a4fd7bd-503e-4e4a-b85e-5501305c7a98"))
			.thenReturn(ServiceResponseBuilder.<Token>ok()
			.withResult(token).build());
		
		when(tokenService.isValidToken("8a4fd7bd-503e-4e4a-b85e-5501305c7a98"))
			.thenReturn(ServiceResponseBuilder.<Boolean>ok()
			.withResult(false).build());
    	
    	List<String> errors = new ArrayList<>();
    	errors.add(applicationContext.getMessage(TokenService.Validations.EXPIRED_TOKEN.getCode(), null, Locale.ENGLISH));
    	
    	getMockMvc().perform(post("/recoverpassword").params(userData))
    		.andDo(print())
			.andExpect(model().attribute("errors", equalTo(errors)))
			.andExpect(model().attribute("isExpired", equalTo(true)));
    }

    @Test
    public void shouldResetPassword() throws Exception {
    	when(tokenService.isValidToken("8a4fd7bd-503e-4e4a-b85e-5501305c7a98"))
			.thenReturn(ServiceResponseBuilder.<Boolean>ok()
			.withResult(true).build());
	
        when(tokenService.getToken("8a4fd7bd-503e-4e4a-b85e-5501305c7a98"))
                .thenReturn(ServiceResponseBuilder.<Token>ok()
                        .withResult(token).build());

        when(userService.findByEmail(token.getUserEmail()))
                .thenReturn(ServiceResponseBuilder.<User>ok()
                        .withResult(user).build());

        when(userService.save(user, "qwertyqwertyqwerty", "qwertyqwertyqwerty"))
                .thenReturn(ServiceResponseBuilder.<User>ok()
                        .withResult(user).build());

        getMockMvc().perform(post("/recoverpassword").params(userData))
                .andDo(print())
                .andExpect(view().name("redirect:/login"));
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

        @Bean
        public CaptchaService captchaService() {
            return Mockito.mock(CaptchaService.class);
        }
    }
}