package com.konkerlabs.platform.registry.test.idm.config;

import com.konkerlabs.platform.registry.idm.business.services.OAuthClientDetailsService;
import com.konkerlabs.platform.registry.test.base.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class
})
public class OAuthClientDetailsServiceTest extends BusinessLayerTestSupport {

    @Autowired
    private OAuthClientDetailsService oauthClientDetailsService;

    @Test
    public void testSaveClient() {

        oauthClientDetailsService.saveClient(null, null, null);

    }

}
