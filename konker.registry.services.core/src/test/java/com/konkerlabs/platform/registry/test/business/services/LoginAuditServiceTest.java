package com.konkerlabs.platform.registry.test.business.services;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.konkerlabs.platform.registry.business.model.LoginAudit;
import com.konkerlabs.platform.registry.business.model.LoginAudit.LoginAuditEvent;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.LoginAuditService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoAuditTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { 
        MongoTestConfiguration.class, 
        MongoAuditTestConfiguration.class,
        BusinessTestConfiguration.class })
public class LoginAuditServiceTest extends BusinessLayerTestSupport {

    @Autowired
    private LoginAuditService loginAuditService;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldListAscendingAndDescending() {

        Tenant tenant = Tenant.builder().id("ddaf1f38").build();
        User user = User.builder().tenant(tenant).email("ddaf1f38").build();

        ServiceResponse<LoginAudit> serviceResponse = loginAuditService.register(tenant, user, LoginAuditEvent.LOGIN);

        Assert.assertTrue(serviceResponse.isOk());

    }

}
