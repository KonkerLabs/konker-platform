package com.konkerlabs.platform.registry.test.web.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import com.konkerlabs.platform.registry.audit.model.TenantLog;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.TenantLogService;
import com.konkerlabs.platform.registry.config.CdnConfig;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.WebConfig;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.converters.DateToStringConverter;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = { WebMvcConfig.class, WebTestConfiguration.class, SecurityTestConfiguration.class,
		TenantLogControllerTest.UserControllerTestContextConfig.class, WebConfig.class,
		CdnConfig.class, EmailConfig.class})
public class TenantLogControllerTest extends WebLayerTestContext {

	@Autowired
	private TenantLogService tenantLogService;

	@Autowired
	private DateToStringConverter dateToStringConverter;

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
		Mockito.reset(tenantLogService);
	}

	@Test
	@WithMockUser(authorities = { "ROLE_SUPER_USER", "ROLE_IOT_USER", "ROLE_ANALYTICS_USER" })
	public void shouldDescendingByDefault() throws Exception {

		ServiceResponse<List<TenantLog>> responseTenantOk = ServiceResponseBuilder.<List<TenantLog>>ok().build();
		responseTenantOk.setResult(new ArrayList<TenantLog>());

		when(tenantLogService.findByTenant(Matchers.anyObject(), Matchers.eq(false))).thenReturn(responseTenantOk);

		ResultActions result = getMockMvc().perform(get("/tenants/log"));

		result.andExpect(model().attribute("logs", org.hamcrest.Matchers.notNullValue()));
		result.andExpect(model().attribute("asc", org.hamcrest.Matchers.equalTo(false)));

	}

	@Test
	@WithMockUser(authorities = { "ROLE_SUPER_USER", "ROLE_IOT_USER", "ROLE_ANALYTICS_USER" })
	public void shouldReturnErrorMessage() throws Exception {

		String line = "Get high, climb a tree.";

		ServiceResponse<List<TenantLog>> responseTenantError = ServiceResponseBuilder.<List<TenantLog>>error().build();
		responseTenantError.setResult(new ArrayList<TenantLog>());

		Map<String, Object[]> responseMessages = new HashMap<>();
		responseMessages.put(line, null);
		responseTenantError.setResponseMessages(responseMessages);

		when(tenantLogService.findByTenant(Matchers.anyObject(), Matchers.eq(false))).thenReturn(responseTenantError);

		ResultActions result = getMockMvc().perform(get("/tenants/log"));

		result.andExpect(model().attribute("logs", org.hamcrest.Matchers.notNullValue()));
		result.andExpect(model().attribute("asc", org.hamcrest.Matchers.equalTo(false)));
		result.andExpect(model().attribute("message", org.hamcrest.Matchers.equalTo(responseMessages)));

	}

	@Test
	@WithMockUser(authorities = { "ROLE_SUPER_USER", "ROLE_IOT_USER", "ROLE_ANALYTICS_USER" })
	public void shouldAscendingTenantLogs() throws Exception {

		ServiceResponse<List<TenantLog>> responseTenantOk = ServiceResponseBuilder.<List<TenantLog>>ok().build();

		List<TenantLog> logs = new ArrayList<TenantLog>();
		logs.add(TenantLog.builder().time(new Date()).build());
		logs.add(TenantLog.builder().time(new Date()).build());
		logs.add(TenantLog.builder().time(new Date()).build());
		responseTenantOk.setResult(logs);

		when(tenantLogService.findByTenant(Matchers.anyObject(), Matchers.eq(true))).thenReturn(responseTenantOk);
		when(dateToStringConverter.convert(Matchers.anyObject())).thenReturn("00/00/0000");

		ResultActions result = getMockMvc().perform(get("/tenants/log").param("asc", "true"));

		result.andExpect(model().attribute("logs", org.hamcrest.Matchers.iterableWithSize(3)));
		result.andExpect(model().attribute("asc", org.hamcrest.Matchers.equalTo(true)));

	}

	@Test
	@WithMockUser(authorities = { "ROLE_SUPER_USER", "ROLE_IOT_USER", "ROLE_ANALYTICS_USER" })
	public void shouldDescendingTenantLogs() throws Exception {

		ServiceResponse<List<TenantLog>> responseTenantOk = ServiceResponseBuilder.<List<TenantLog>>ok().build();

		List<TenantLog> logs = new ArrayList<TenantLog>();
		logs.add(TenantLog.builder().time(new Date()).build());
		logs.add(TenantLog.builder().time(new Date()).build());
		responseTenantOk.setResult(logs);

		when(tenantLogService.findByTenant(Matchers.anyObject(), Matchers.eq(false))).thenReturn(responseTenantOk);
		when(dateToStringConverter.convert(Matchers.anyObject())).thenReturn("00/00/0000");

		ResultActions result = getMockMvc().perform(get("/tenants/log").param("asc", "false"));

		result.andExpect(model().attribute("logs", org.hamcrest.Matchers.iterableWithSize(2)));
		result.andExpect(model().attribute("asc", org.hamcrest.Matchers.equalTo(false)));

	}

	@Configuration
	static class UserControllerTestContextConfig {

		@Bean
		public TenantLogService tenantLogService() {
			return Mockito.mock(TenantLogService.class);
		}

		@Bean
		public DateToStringConverter dateToStringConverter() {
			return Mockito.mock(DateToStringConverter.class);
		}

	}

}