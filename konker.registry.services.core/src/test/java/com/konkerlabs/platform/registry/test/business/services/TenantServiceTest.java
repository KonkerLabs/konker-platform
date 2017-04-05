package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TenantService;
import com.konkerlabs.platform.registry.business.services.api.TenantService.Validations;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoTestConfiguration.class, BusinessTestConfiguration.class })
@UsingDataSet(locations = { "/fixtures/tenants.json", "/fixtures/users.json", "/fixtures/passwordBlacklist.json" })
public class TenantServiceTest extends BusinessLayerTestSupport {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	private TenantService tenantService;

	@Autowired
	private TenantRepository tenantRepository;

	@Autowired
	private DeviceRepository deviceRepository;

	private Tenant tenant;

	@Before
	public void setUp() throws Exception {
		tenant = tenantRepository.findByDomainName("konker");
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void shouldUpdateLogLevel() {

		// set disabled
		tenantService.updateLogLevel(tenant, LogLevel.DISABLED);

		Tenant stored = tenantRepository.findOne(tenant.getId());

		Assert.assertNotNull(stored);
		Assert.assertEquals(stored.getLogLevel(), LogLevel.DISABLED);

		tenantService.updateLogLevel(tenant, LogLevel.DISABLED);

		// change to all
		ServiceResponse<Tenant> response = tenantService.updateLogLevel(tenant, LogLevel.ALL);

		stored = tenantRepository.findOne(tenant.getId());

		Assert.assertNotNull(stored);
		Assert.assertEquals(stored.getLogLevel(), LogLevel.ALL);
		Assert.assertEquals(response.getStatus(), ServiceResponse.Status.OK);

	}

	@Test
	public void shouldWarningLogLevelBeDefault() {

		ServiceResponse<Tenant> response = tenantService.updateLogLevel(tenant, null);

		Tenant stored = tenantRepository.findOne(tenant.getId());

		Assert.assertNotNull(stored);
		Assert.assertEquals(stored.getLogLevel(), LogLevel.WARNING);
		Assert.assertEquals(response.getStatus(), ServiceResponse.Status.OK);

	}

	@Test
	public void shouldValidateNullTenant() {

		ServiceResponse<Tenant> response = tenantService.updateLogLevel(null, LogLevel.WARNING);

		Assert.assertNotNull(response);
		Assert.assertEquals(response.getStatus(), ServiceResponse.Status.ERROR);
		Assert.assertTrue(response.getResponseMessages().containsKey(Validations.TENANT_NULL.getCode()));

	}

	@Test
	public void shouldValidateNonExistingTenant() {

		Tenant strangeTenant = Tenant.builder().id("qqqq").build();
		ServiceResponse<Tenant> response = tenantService.updateLogLevel(strangeTenant, LogLevel.WARNING);

		Assert.assertNotNull(response);
		Assert.assertEquals(response.getStatus(), ServiceResponse.Status.ERROR);
		Assert.assertTrue(response.getResponseMessages().containsKey(Validations.NO_EXIST_TENANT.getCode()));

	}

	@Test
	public void shouldChangeDevicesLogLevels() {

		// set to INFO
		tenantService.updateLogLevel(tenant, LogLevel.INFO);

		String deviceId = "Q3UuYLFN67";
		Device device = Device.builder().logLevel(LogLevel.INFO).description(deviceId).deviceId(deviceId).tenant(tenant)
				.build();
		deviceRepository.save(device);

		// set to DISABLED
		tenantService.updateLogLevel(tenant, LogLevel.DISABLED);

		device = deviceRepository.findByTenantIdAndDeviceId(tenant.getId(), deviceId);

		Assert.assertNotNull(device);
		Assert.assertEquals(device.getLogLevel(), LogLevel.DISABLED);

	}

}
