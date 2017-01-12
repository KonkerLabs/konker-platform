package com.konkerlabs.platform.registry.test.audit.repositories;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.konkerlabs.platform.registry.audit.model.TenantLog;
import com.konkerlabs.platform.registry.audit.repositories.TenantLogRepository;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.base.RedisTestConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { BusinessTestConfiguration.class, 
		MongoTestConfiguration.class,
		RedisTestConfiguration.class })
public class TenantLogRepositoryTest {

	@Autowired
	private TenantLogRepository tenantLogRepository;

	@Test
	public void shouldInsertLogs() {

		Tenant tenant = Tenant.builder().domainName("7urBfIo2wj").build();

		int initialSize = tenantLogRepository.findAll(tenant).size();

		// assert empty collection
		Assert.assertEquals(0, initialSize);

		for (int i = 0; i < 10; i++) {
			tenantLogRepository.insert(tenant.getDomainName(), new Date(), "I4PlMe5TbK");
		}

		List<TenantLog> logs = tenantLogRepository.findAll(tenant);

		Assert.assertEquals(10, logs.size());

	}

	@Test
	public void shouldInsertLogsInMilliseconds() {

		Tenant tenant = Tenant.builder().domainName("KBCYiVE379").build();

		int initialSize = tenantLogRepository.findAll(tenant).size();

		// assert empty collection
		Assert.assertEquals(0, initialSize);

		for (int i = 0; i < 10; i++) {
			tenantLogRepository.insert(tenant.getDomainName(), new Date().getTime(), "LuxUkmRSB9");
		}

		List<TenantLog> logs = tenantLogRepository.findAll(tenant);

		Assert.assertEquals(10, logs.size());

	}

	@Test
	public void shouldGetInstanceRepositoryWork() {

		TenantLogRepository repository = TenantLogRepository.getInstance();
		repository.insert("YJgQ8Zj2j0", new Date().getTime(), "H5ITwKKqrm");

	}

}