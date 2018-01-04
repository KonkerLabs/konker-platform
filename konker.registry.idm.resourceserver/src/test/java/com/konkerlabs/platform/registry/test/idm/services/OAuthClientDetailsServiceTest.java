package com.konkerlabs.platform.registry.test.idm.services;

import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Gateway;
import com.konkerlabs.platform.registry.business.model.OauthClientDetails;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.OauthClientDetailRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.idm.services.OAuthClientDetailsService;
import com.konkerlabs.platform.registry.test.base.*;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json"})
public class OAuthClientDetailsServiceTest extends BusinessLayerTestSupport {

    @Autowired
    private OAuthClientDetailsService oauthClientDetailsService;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant tenant;

    @Before
    public void setUp() {
        tenant = tenantRepository.findByDomainName("konker");
    }

    @Test
    public void trySaveClientWithInvalidClientId() {
        OauthClientDetails clientDetails = new OauthClientDetails();

        ServiceResponse<OauthClientDetails> serviceResponse = oauthClientDetailsService.saveClient(tenant, null, clientDetails);
        assertThat(serviceResponse, hasErrorMessage(OAuthClientDetailsService.Validations.INVALID_DETAILS.getCode()));
    }

    @Test
    public void saveAndLoadGatewayClient() {
        Gateway gateway = new Gateway();
        gateway.setId("gateway-id");
        gateway.setGuid("gateway-guid");
        gateway.setTenant(tenant);

        OauthClientDetails clientDetails = new OauthClientDetails();
        clientDetails.setGatewayProperties(gateway);

        ServiceResponse<OauthClientDetails> serviceResponse = oauthClientDetailsService.saveClient(tenant, null, clientDetails);
        assertThat(serviceResponse, isResponseOk());

        ClientDetails clientDetailsDB = oauthClientDetailsService.loadClientByClientId(gateway.getRoutUriTemplate());
        assertThat(clientDetailsDB, notNullValue());

    }

}
