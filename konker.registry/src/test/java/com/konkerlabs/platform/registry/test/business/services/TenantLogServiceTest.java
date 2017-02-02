package com.konkerlabs.platform.registry.test.business.services;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static org.hamcrest.MatcherAssert.assertThat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.konkerlabs.platform.registry.audit.model.TenantLog;
import com.konkerlabs.platform.registry.audit.repositories.TenantLogRepository;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TenantLogService;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;

import ch.qos.logback.classic.Level;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
		BusinessTestConfiguration.class
})
public class TenantLogServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
	private TenantLogService tenantLogService;

	@Autowired
	private TenantLogRepository tenantLogRepository;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
	public void shouldListAscendingAndDescending() {

		String domainName = "Fo8RmPoLWz";

		tenantLogRepository.insert(domainName, new Date(1484219387000L), Level.WARN.levelStr, "1");
		tenantLogRepository.insert(domainName, new Date(1484219388000L), Level.INFO.levelStr, "2");
		tenantLogRepository.insert(domainName, new Date(1484219389000L), Level.ERROR.levelStr, "3");

		Tenant tenant = Tenant.builder().domainName(domainName).build();

		ServiceResponse<List<TenantLog>> response = tenantLogService.findByTenant(tenant, true);

		List<TenantLog> logs = response.getResult();

		Assert.assertEquals("1", logs.get(0).getMessage());
		Assert.assertEquals("2", logs.get(1).getMessage());
		Assert.assertEquals("3", logs.get(2).getMessage());

		Assert.assertEquals("WARN", logs.get(0).getLevel());
		Assert.assertEquals("INFO", logs.get(1).getLevel());
		Assert.assertEquals("ERROR", logs.get(2).getLevel());

		// Descending

		response = tenantLogService.findByTenant(tenant, false);

		logs = response.getResult();

		Assert.assertEquals("3", logs.get(0).getMessage());
		Assert.assertEquals("2", logs.get(1).getMessage());
		Assert.assertEquals("1", logs.get(2).getMessage());

		Assert.assertEquals("ERROR", logs.get(0).getLevel());
		Assert.assertEquals("INFO", logs.get(1).getLevel());
		Assert.assertEquals("WARN", logs.get(2).getLevel());

    }

	@Test
	public void shouldPersistAndRetrieveSameDate() throws ParseException {

		String domainName = "XJhqUNUvBZ";

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

		String level = Level.WARN.levelStr;
		String dataText = "2016-08-20 17:45:31";

		tenantLogRepository.insert(domainName, df.parse(dataText), level, dataText);

		Tenant tenant = Tenant.builder().domainName(domainName).build();

		ServiceResponse<List<TenantLog>> response = tenantLogService.findByTenant(tenant, true);

		List<TenantLog> logs = response.getResult();

		Assert.assertEquals(dataText, df.format(logs.get(0).getTime()));

	}

	@Test
	public void shouldReturnResponseMessagesIfTenantIsNull() throws Exception {
		ServiceResponse<List<TenantLog>> response = tenantLogService.findByTenant(null, true);

		assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
	}

}
