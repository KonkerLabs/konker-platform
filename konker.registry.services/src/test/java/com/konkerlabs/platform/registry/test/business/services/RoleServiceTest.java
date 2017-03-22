package com.konkerlabs.platform.registry.test.business.services;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;

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

import com.konkerlabs.platform.registry.business.model.Role;
import com.konkerlabs.platform.registry.business.repositories.RoleRepository;
import com.konkerlabs.platform.registry.business.services.api.RoleService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class
})
@UsingDataSet(locations = {
        "/fixtures/roles.json"
})
public class RoleServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private RoleService roleService;

    private Role roleIOT;
    private Role roleAnalyt;
    private Role roleSuper;

    @Before
    public void setUp() throws Exception {
    	roleIOT = roleRepository.findByName("ROLE_IOT_USER");
    	roleAnalyt = roleRepository.findByName("ROLE_ANALYTICS_USER");
    	roleSuper = roleRepository.findByName("ROLE_SUPER_USER");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldReturnRoleByName() {
    	ServiceResponse<Role> roleIotUser = roleService.findByName("ROLE_IOT_USER");
    	ServiceResponse<Role> roleAnalytics = roleService.findByName("ROLE_ANALYTICS_USER");
    	ServiceResponse<Role> roleSuperUser = roleService.findByName("ROLE_SUPER_USER");
    	
    	Assert.assertNotNull(roleIotUser);
    	Assert.assertNotNull(roleAnalytics);
    	Assert.assertNotNull(roleSuperUser);
    	
    	Assert.assertThat(roleIotUser, isResponseOk());
    	Assert.assertThat(roleAnalytics, isResponseOk());
    	Assert.assertThat(roleSuperUser, isResponseOk());
    	
    	Assert.assertEquals(roleIOT, roleIotUser.getResult());
    	Assert.assertEquals(roleAnalyt, roleAnalytics.getResult());
    	Assert.assertEquals(roleSuper, roleSuperUser.getResult());
    }
    
    @Test
    public void shouldReturnErroWhenFindByInvalidName() {
    	ServiceResponse<Role> roleIotUser = roleService.findByName("ROLE_IOT_USERS");
    	ServiceResponse<Role> roleAnalytics = roleService.findByName("ROLE_ANALYTICS_USERS");
    	ServiceResponse<Role> roleSuperUser = roleService.findByName("ROLE_SUPER_USERS");
    	
    	Assert.assertThat(roleIotUser, hasErrorMessage(RoleService.Validations.ROLE_NOT_EXIST.getCode()));
    	Assert.assertThat(roleAnalytics, hasErrorMessage(RoleService.Validations.ROLE_NOT_EXIST.getCode()));
    	Assert.assertThat(roleSuperUser, hasErrorMessage(RoleService.Validations.ROLE_NOT_EXIST.getCode()));
    }
   
}
