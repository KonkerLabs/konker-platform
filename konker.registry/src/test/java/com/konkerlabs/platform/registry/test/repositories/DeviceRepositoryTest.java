package com.konkerlabs.platform.registry.test.repositories;

import com.konkerlabs.platform.registry.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.repositories.TenantRepository;
import com.konkerlabs.platform.registry.test.base.MongoIntegrationTestBase;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json"})
public class DeviceRepositoryTest extends MongoIntegrationTestBase {

    @Autowired
    private DeviceRepository repository;

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    public void testMongo() throws Exception {
        System.out.println(repository.findAll());
    }
}
