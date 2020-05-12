package com.konkerlabs.platform.registry.test.web.controllers;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

import com.konkerlabs.platform.registry.business.model.KonkerIuguCharge;
import com.konkerlabs.platform.registry.business.services.api.*;
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

import com.konkerlabs.platform.registry.billing.model.TenantDailyUsage;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.config.CdnConfig;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.MessageSourceConfig;
import com.konkerlabs.platform.registry.config.WebConfig;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.security.TenantUserDetailsService;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.converters.utils.ConverterUtils;
import com.konkerlabs.platform.registry.web.services.api.AvatarService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = { WebMvcConfig.class, WebTestConfiguration.class, SecurityTestConfiguration.class,
		UserControllerTest.UserControllerTestContextConfig.class,
        WebConfig.class, CdnConfig.class, EmailConfig.class, MessageSourceConfig.class })
public class UserControllerTest extends WebLayerTestContext {

	@Autowired
	private UserService userService;

	@Autowired
	private TenantService tenantService;

	@Autowired
	private IuguService iuguService;

	@Autowired
	private AvatarService avatarService;

	@Autowired
	private User user;

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
		Mockito.reset(userService);
		Mockito.reset(tenantService);
		Mockito.reset(avatarService);
		Mockito.reset(iuguService);
	}

	@Test
	@WithMockUser(authorities = { "ROLE_SUPER_USER", "ROLE_IOT_USER", "ROLE_ANALYTICS_USER" })
	public void shouldOpenViewForm() throws Exception {
		when(tenantService.findTenantDailyUsage(Matchers.anyObject()))
			.thenReturn(ServiceResponseBuilder.<List<TenantDailyUsage>> ok()
					.withResult(Collections.singletonList(
							TenantDailyUsage.builder()
								.tenantDomain("konker")
								.incomingPayloadSize(100)
								.outgoingPayloadSize(128)
								.build()))
					.build());

		when(iuguService.findNextCharge(anyObject()))
				.thenReturn(ServiceResponseBuilder.<KonkerIuguCharge> ok()
						.withResult(KonkerIuguCharge.builder()
										.maskedCardNumber("xxxx xxxx xxxx 1111")
										.nextCharge("01 Jun, 2020")
										.nextChargeValue("R$ 1,99")
										.build())
						.build());

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

		when(avatarService.updateAvatar(Matchers.anyObject())).thenReturn(responseOk);
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

		when(avatarService.updateAvatar(Matchers.anyObject())).thenReturn(responseOk);
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

		@Bean
		public IuguService iuguService() {
			return Mockito.mock(IuguService.class);
		}

	}

}