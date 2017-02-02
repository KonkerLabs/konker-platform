package com.konkerlabs.platform.registry.test.audit.repositories;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.konkerlabs.platform.registry.audit.model.TenantLog;
import com.konkerlabs.platform.registry.audit.repositories.TenantLogRepository;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.base.RedisTestConfiguration;

import ch.qos.logback.classic.Level;

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
			tenantLogRepository.insert(tenant.getDomainName(), new Date(), Level.WARN.levelStr, "I4PlMe5TbK");
		}

		List<TenantLog> logs = tenantLogRepository.findAll(tenant);

		Assert.assertEquals(10, logs.size());
		Assert.assertEquals("WARN", logs.get(0).getLevel());

	}

	@Test
	public void shouldInsertLogsInMilliseconds() {

		Tenant tenant = Tenant.builder().domainName("KBCYiVE379").build();

		int initialSize = tenantLogRepository.findAll(tenant).size();

		// assert empty collection
		Assert.assertEquals(0, initialSize);

		for (int i = 0; i < 10; i++) {
			tenantLogRepository.insert(tenant.getDomainName(), new Date().getTime(), Level.INFO.levelStr, "LuxUkmRSB9");
		}

		List<TenantLog> logs = tenantLogRepository.findAll(tenant);

		Assert.assertEquals(10, logs.size());
		Assert.assertEquals("INFO", logs.get(0).getLevel());

	}

	@Test
	public void shouldGetInstanceRepositoryWork()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		// workaround to get valid mongo template
		Field f = tenantLogRepository.getClass().getDeclaredField("mongoAuditTemplate");
		f.setAccessible(true);
		MongoTemplate mongoTemplate = (MongoTemplate) f.get(tenantLogRepository);

		TenantLogRepository repository = TenantLogRepository.getInstance();

		// workaround to set mongo template
		Field fSet = repository.getClass().getDeclaredField("mongoAuditTemplate");
		fSet.setAccessible(true);
		fSet.set(repository, mongoTemplate);

		repository.insert("YJgQ8Zj2j0", new Date().getTime(), Level.WARN.levelStr, "H5ITwKKqrm");

	}

}