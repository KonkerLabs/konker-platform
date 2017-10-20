package com.konkerlabs.platform.registry.test.business.services;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Instant;
import java.util.List;

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

import com.konkerlabs.platform.registry.billing.model.TenantDailyUsage;
import com.konkerlabs.platform.registry.billing.repositories.TenantDailyUsageRepository;
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
import com.konkerlabs.platform.registry.test.base.MongoBillingTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoTestConfiguration.class, BusinessTestConfiguration.class, MongoBillingTestConfiguration.class })
@UsingDataSet(locations = { "/fixtures/tenants.json", "/fixtures/users.json", "/fixtures/passwordBlacklist.json", "/fixtures/tenantDailyUsage.json" })
public class TenantServiceTest extends BusinessLayerTestSupport {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	private TenantService tenantService;

	@Autowired
	private TenantRepository tenantRepository;

	@Autowired
	private DeviceRepository deviceRepository;
	
	@Autowired
	private TenantDailyUsageRepository tenantDailyUsageRepository;
	
	private Tenant tenant;

	@Before
	public void setUp() throws Exception {
		tenant = tenantRepository.findByDomainName("konker");
		List<TenantDailyUsage> usages = tenantDailyUsageRepository.findAllByTenantDomain("konker");
		
		tenantDailyUsageRepository.save(TenantDailyUsage.builder()
				.date(Instant.now())
				.incomingDevices(4)
				.incomingEventsCount(200)
				.incomingPayloadSize(512)
				.outgoingDevices(2)
				.outgoingEventsCount(200)
				.outgoingPayloadSize(680)
				.tenantDomain(tenant.getDomainName())
				.build());
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
	
	@Test
	public void shouldFindTenantDailyUsage() {
		ServiceResponse<List<TenantDailyUsage>> responseService = tenantService.findTenantDailyUsage(tenant);
		
		Assert.assertNotNull(responseService);
		Assert.assertEquals(responseService.getStatus(), ServiceResponse.Status.OK);
		Assert.assertEquals(responseService.getResult().size(), 1);
	}

	@Test
	public void shouldReturnErrorForTenantNull() {
		ServiceResponse<Tenant> serviceResponse = tenantService.save(null);
		
		Assert.assertNotNull(serviceResponse);
        assertThat(
                serviceResponse,
                hasErrorMessage(TenantService.Validations.TENANT_NULL.getCode()));
	}
	
	@Test
	public void shouldReturnErrorForTenantNameNull() {
		tenant.setName(null);
		ServiceResponse<Tenant> serviceResponse = tenantService.save(tenant);
		
		Assert.assertNotNull(serviceResponse);
        assertThat(
                serviceResponse,
                hasErrorMessage(TenantService.Validations.TENANT_NAME_NULL.getCode()));
        
        tenant.setName("");
        serviceResponse = tenantService.save(tenant);
		
		Assert.assertNotNull(serviceResponse);
        assertThat(
                serviceResponse,
                hasErrorMessage(TenantService.Validations.TENANT_NAME_NULL.getCode()));
	}
	
	@Test
	public void shouldSaveTenant() {
		ServiceResponse<Tenant> serviceResponse = tenantService.save(tenant);
		
		Assert.assertNotNull(serviceResponse);
		Assert.assertEquals(serviceResponse.getStatus(), ServiceResponse.Status.OK);
		Assert.assertEquals(serviceResponse.getResult().getDevicesLimit().longValue(), 5l);
	}
	
}
