package com.konkerlabs.platform.registry.test.web.controllers;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import java.util.Collections;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TenantService;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.config.CdnConfig;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.HotjarConfig;
import com.konkerlabs.platform.registry.config.WebConfig;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.security.TenantUserDetailsService;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.controllers.UserPersonificationController.Messages;
import com.konkerlabs.platform.registry.web.converters.utils.ConverterUtils;
import com.konkerlabs.platform.registry.web.services.api.AvatarService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = { WebMvcConfig.class, WebTestConfiguration.class, SecurityTestConfiguration.class,
		UserPersonificationControllerTest.UserControllerTestContextConfig.class,
        WebConfig.class, HotjarConfig.class, CdnConfig.class, EmailConfig.class })
public class UserPersonificationControllerTest extends WebLayerTestContext {

	@Autowired
	private UserService userService;

	@Autowired
	private Tenant tenant;
	
	private User otherUser;
	
	@Autowired
	private ApplicationContext applicationContext;

	@Before
	public void setUp() {
		otherUser = User.builder()
				.email("master@mydomain.com")
				.zoneId(TimeZone.AMERICA_SAO_PAULO)
				.language(Language.EN)
				.avatar("default.jpg")
				.dateFormat(DateFormat.YYYYMMDD)
				.roles(Collections.emptyList())
				.tenant(tenant).build();
	}

	@After
	public void tearDown() {
		Mockito.reset(userService);
	}

	@Test
	@WithMockUser(authorities = { "USER_PERSONIFICATION" })
	public void shouldOpenViewForm() throws Exception {
		ResultActions result = getMockMvc().perform(get("/personification"));

		result.andExpect(model().attribute("user", notNullValue()))
			.andExpect(model().attribute("action", containsString("/personification")));
	}
	
	@Test
	@WithMockUser(authorities = { "USER_PERSONIFICATION" })
	public void shouldReturnErrorUserNotFound() throws Exception {

		ServiceResponse<User> responseError = ServiceResponseBuilder.<User>error()
				.withMessage(applicationContext.getMessage(Messages.USER_PERSONIFICATION_NOT_FOUND.getCode(), null, Locale.US))
				.build();

		when(userService.findByEmail(Matchers.anyString()))
				.thenReturn(responseError);

		ResultActions result = getMockMvc().perform(post("/personification"));
		result.andExpect(flash().attribute("errors", contains(
				applicationContext.getMessage(Messages.USER_PERSONIFICATION_NOT_FOUND.getCode(), null, Locale.US))));
	}
	
	@Test
	@WithMockUser(authorities = { "USER_PERSONIFICATION" })
	public void shouldPersonificateUser() throws Exception {
		ServiceResponse<User> responseOk = ServiceResponseBuilder.<User>ok()
				.withResult(otherUser)
				.withMessage(applicationContext.getMessage(Messages.USER_PERSONIFICATED_SUCCESSFULLY.getCode(), null, Locale.US))
				.build();

		when(userService.findByEmail(Matchers.anyString()))
				.thenReturn(responseOk);

		ResultActions result = getMockMvc().perform(post("/personification"));
		result.andExpect(flash().attribute("message", containsString(
				applicationContext.getMessage(Messages.USER_PERSONIFICATED_SUCCESSFULLY.getCode(), null, Locale.US))));
		
		Assert.assertEquals(otherUser, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
	}

	@Configuration
	static class UserControllerTestContextConfig {

		@Bean
		public TenantUserDetailsService tenantUserDetailsService() {
			return Mockito.mock(TenantUserDetailsService.class);
		}

		@Bean
		public UserService userService() {
			return Mockito.mock(UserService.class);
		}

		@Bean
		public AvatarService avatarService() {
			return Mockito.mock(AvatarService.class);
		}

		@Bean
		public TenantService tenantService() {
			return Mockito.mock(TenantService.class);
		}

		@Bean
		public ConverterUtils converterUtils() {
			return Mockito.mock(ConverterUtils.class);
		}

	}

}