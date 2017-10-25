package com.konkerlabs.platform.registry.test.web.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Token;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.User.JobEnum;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.services.api.EmailService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TokenService;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.HotjarConfig;
import com.konkerlabs.platform.registry.config.WebConfig;
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
        UserSubscriptionControllerTest.EmailConfirmationTestContextConfig.class, 
        WebConfig.class,
        EmailConfig.class, 
        HotjarConfig.class
})
@ActiveProfiles("email")
public class UserSubscriptionControllerTest extends WebLayerTestContext {

    private static final String USER_EMAIL = "user@testdomain.com";
    private static final String USER_EMAIL_INVALID = "commontestdomain.com";
    


    @Autowired
    ApplicationContext applicationContext;
    
    @Autowired
    EmailService emailService;
    
    @Autowired
    TokenService tokenService;


    @Autowired
    UserService userService;
    

    private User user;

    private Token token;
    private Token invalidToken;
    private MultiValueMap<String, String> userData;
    
    @Before
    public void setUp() {
    	token = Token.builder()
    				.creationDateTime(Instant.now())
    				.isExpired(false)
    				.purpose(TokenService.Purpose.VALIDATE_EMAIL.name())
    				.token("8a4fd7bd-503e-4e4a-b85e-5501305c7a98")
    				.userEmail("user@testdomain.com")
    				.build();
    	
    	invalidToken = Token.builder()
				.creationDateTime(Instant.now())
				.isExpired(true)
				.purpose(TokenService.Purpose.VALIDATE_EMAIL.name())
				.token("8a4fd7bd-503e-4e4a-b85e-5501305c7a99")
				.userEmail("user@testdomain.com")
				.build();
    	
    	user = User.builder()
    			.name("konker")
    			.email(USER_EMAIL)
    			.language(Language.EN)
    			.build();
    	
    	userData = new LinkedMultiValueMap<>();
    	userData.add("email", user.getEmail());
    	userData.add("name", user.getName());
    	userData.add("username", user.getUsername());
    	userData.add("tenantName", user.getName());
    	userData.add("newPassword", "qwertyqwertyqwerty");
    	userData.add("newPasswordConfirmation", "qwertyqwertyqwerty");

    }

    @After
    public void tearDown() {

    }
    
    @Test
    public void shouldReturnToFormAnyError() throws Exception {
    	user.setEmail(USER_EMAIL_INVALID);
    	user.setTenant(Tenant.builder().name(user.getName()).build());
    	
        when(userService.createAccount(user, "qwertyqwertyqwerty", "qwertyqwertyqwerty"))
    		.thenReturn(
    				ServiceResponseBuilder.<User>error()
    					.withMessage(UserService.Validations.INVALID_USER_EMAIL.getCode())
    					.build());
    	
        
        userData.set("email", USER_EMAIL_INVALID);
        List<String> errors = new ArrayList<>();
    	errors.add(applicationContext.getMessage(UserService.Validations.INVALID_USER_EMAIL.getCode(), null, Locale.ENGLISH));
        
    	getMockMvc().perform(post("/subscription")
    			.params(userData))
    		.andDo(print())
    		.andExpect(view().name("subscription/form"))
    		.andExpect(model().attribute("errors", equalTo(errors)));
    }
    
    @Test
    public void shouldRedirectWhenSaveUser() throws Exception {
    	user.setTenant(Tenant.builder().name(user.getName()).build());
        when(userService.createAccount(user, "qwertyqwertyqwerty", "qwertyqwertyqwerty"))
    		.thenReturn(ServiceResponseBuilder.<User>ok()
    		.withResult(user).build());
    	
        getMockMvc().perform(post("/subscription")
    			.params(userData))
    		.andDo(print())
    		.andExpect(redirectedUrl("/subscription/successpage"));
    }
    
    @Test
    public void shouldRaiseAnExceptionIfTokenExpired() throws Exception {
    	when(tokenService.getToken("8a4fd7bd-503e-4e4a-b85e-5501305c7a99"))
			.thenReturn(ServiceResponseBuilder.<Token>ok()
			.withResult(invalidToken).build());
    	
    	List<String> errors = new ArrayList<>();
    	errors.add(applicationContext.getMessage(TokenService.Validations.EXPIRED_TOKEN.getCode(), null, Locale.ENGLISH));
    	
    	getMockMvc().perform(get("/subscription/8a4fd7bd-503e-4e4a-b85e-5501305c7a99"))
    		.andDo(print())
			.andExpect(model().attribute("errors", equalTo(errors)));
    }
    
    @Test
    public void shouldRaiseAnExceptionIfTokenInvalid() throws Exception {
    	when(tokenService.getToken("8a4fd7bd-503e-4e4a-b85e-5501305c7a99"))
			.thenReturn(ServiceResponseBuilder.<Token>error()
			.withMessage(TokenService.Validations.INVALID_TOKEN.getCode())		
			.withResult(null).build());
    	
    	List<String> errors = new ArrayList<>();
    	errors.add(applicationContext.getMessage(TokenService.Validations.INVALID_TOKEN.getCode(), null, Locale.ENGLISH));
    	
    	getMockMvc().perform(get("/subscription/8a4fd7bd-503e-4e4a-b85e-5501305c7a99"))
    		.andDo(print())
			.andExpect(model().attribute("errors", equalTo(errors)));
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
    	
    	getMockMvc().perform(get("/subscription/8a4fd7bd-503e-4e4a-b85e-5501305c7a98"))
    		.andDo(print())
			.andExpect(model().attribute("errors", equalTo(errors)));
    }
    
    @Test
    public void shouldShowSuccessPage() throws Exception {
            
    	getMockMvc().perform(get("/subscription/successpage"))
    		.andExpect(view().name("subscription/success"));
    }
    
    @Test
    public void shouldShowFormPage() throws Exception {
            
    	getMockMvc().perform(get("/subscription"))
    		.andExpect(view().name("subscription/form"))
    		.andExpect(model().attribute("allJobs", equalTo(JobEnum.values())))
    		.andExpect(model().attribute("action", equalTo("/subscription")));
    }
 
    @Configuration
    static class EmailConfirmationTestContextConfig {
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