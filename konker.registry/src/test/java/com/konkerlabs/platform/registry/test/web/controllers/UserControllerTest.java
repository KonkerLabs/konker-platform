package com.konkerlabs.platform.registry.test.web.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

import org.mockito.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TenantService;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.config.CdnConfig;
import com.konkerlabs.platform.registry.config.HotjarConfig;
import com.konkerlabs.platform.registry.config.WebConfig;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.security.TenantUserDetailsService;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.converters.utils.ConverterUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = { WebMvcConfig.class, WebTestConfiguration.class, SecurityTestConfiguration.class,
		UserControllerTest.UserControllerTestContextConfig.class,
        WebConfig.class, HotjarConfig.class, CdnConfig.class })
public class UserControllerTest extends WebLayerTestContext {

	@Autowired
	private UserService userService;

	@Autowired
	private TenantService tenantService;

	@Autowired
	private User user;

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
		Mockito.reset(userService);
		Mockito.reset(tenantService);
	}

	@Test
	@WithMockUser(authorities = { "ROLE_SUPER_USER", "ROLE_IOT_USER", "ROLE_ANALYTICS_USER" })
	public void shouldOpenViewForm() throws Exception {
		ResultActions result = getMockMvc().perform(get("/me"));

		result.andExpect(model().attribute("loglevels", org.hamcrest.Matchers.notNullValue()));
		result.andExpect(
				model().attribute("loglevels", org.hamcrest.Matchers.arrayContainingInAnyOrder(LogLevel.values())));
	}

	@Test
	@WithMockUser(authorities = { "ROLE_SUPER_USER", "ROLE_IOT_USER", "ROLE_ANALYTICS_USER" })
	public void shouldChangeTenant() throws Exception {

		ServiceResponse<User> responseOk = ServiceResponseBuilder.<User>ok().build();
		ServiceResponse<Tenant> responseTenantOk = ServiceResponseBuilder.<Tenant>ok().build();

		when(userService.save(Matchers.anyObject(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString()))
				.thenReturn(responseOk);
		when(tenantService.updateLogLevel(Matchers.anyObject(), Matchers.anyObject())).thenReturn(responseTenantOk);

		user.getTenant().setLogLevel(LogLevel.ALL);

		ResultActions result = getMockMvc().perform(post("/me"));
		result.andExpect(flash().attribute("message", org.hamcrest.Matchers.containsString("success")));

		Assert.assertEquals(LogLevel.WARNING, user.getTenant().getLogLevel());

	}

	@Test
	@WithMockUser(authorities = { "ROLE_SUPER_USER", "ROLE_IOT_USER", "ROLE_ANALYTICS_USER" })
	public void shouldChangeTenantWithException() throws Exception {

		ServiceResponse<User> responseOk = ServiceResponseBuilder.<User>ok().build();
		ServiceResponse<Tenant> responseTenantOk = ServiceResponseBuilder.<Tenant>error()
				.withMessage(CommonValidations.RECORD_NULL.getCode()).build();

		when(userService.save(Matchers.anyObject(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString()))
				.thenReturn(responseOk);
		when(tenantService.updateLogLevel(Matchers.anyObject(), Matchers.anyObject())).thenReturn(responseTenantOk);

		user.getTenant().setLogLevel(LogLevel.ALL);

		ResultActions result = getMockMvc().perform(post("/me"));
		result.andExpect(flash().attribute("errors", org.hamcrest.Matchers.notNullValue()));
		result.andExpect(flash().attribute("message", org.hamcrest.Matchers.nullValue()));

		// not changed
		Assert.assertEquals(LogLevel.ALL, user.getTenant().getLogLevel());

	}
	
	@Configuration
	static class UserControllerTestContextConfig {

		@Bean
		public TenantUserDetailsService tenantUserDetailsService() {
			return Mockito.mock(TenantUserDetailsService.class);
		}

		@Bean
		public UserService uerService() {
			return Mockito.mock(UserService.class);
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