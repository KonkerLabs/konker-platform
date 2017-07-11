package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.billing.model.TenantDailyUsage;
import com.konkerlabs.platform.registry.billing.repositories.TenantDailyUsageRepository;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TenantBillingService;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoBillingTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoTestConfiguration.class, BusinessTestConfiguration.class, MongoBillingTestConfiguration.class })
@UsingDataSet(locations = { "/fixtures/tenants.json", "/fixtures/users.json", "/fixtures/passwordBlacklist.json", "/fixtures/tenantDailyUsage.json" })
public class TenantBillingServiceTest extends BusinessLayerTestSupport {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	private TenantBillingService tenantBillingService;

	@Autowired
	private TenantDailyUsageRepository tenantDailyUsageRepository;
	
	private Tenant tenant;

	@Before
	public void setUp() throws Exception {
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
	public void tearDown() throws Exception {}
	@Test
	public void shouldFindTenantDailyUsage() {
		ServiceResponse<List<TenantDailyUsage>> responseService = tenantBillingService.findTenantDailyUsage(tenant);
		
		Assert.assertNotNull(responseService);
		Assert.assertEquals(responseService.getStatus(), ServiceResponse.Status.OK);
		Assert.assertEquals(responseService.getResult().size(), 1);
	}

}
