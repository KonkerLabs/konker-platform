
package com.konkerlabs.platform.registry.idm.oauth.mongo.service;


import com.konkerlabs.platform.registry.idm.oauth.mongo.repository.OauthClientDetails;
import com.konkerlabs.platform.registry.idm.oauth.mongo.repository.OauthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.util.Assert;



public class MongoClientDetailsService implements ClientDetailsService, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(MongoClientDetailsService.class);

    private OauthRepository oauthRepository;


    public MongoClientDetailsService() {
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        final OauthClientDetails oauthClientDetails = oauthRepository.findOauthClientDetails(clientId);
        if (oauthClientDetails == null || oauthClientDetails.archived()) {
            LOG.warn("Not found ClientDetails by clientId '{}', because null or archived", clientId);
            throw new ClientRegistrationException("Not found ClientDetails by clientId '" + clientId + "', because null or archived");
        }
        return oauthClientDetails.toClientDetails();
    }


    public void setOauthRepository(OauthRepository oauthRepository) {
        this.oauthRepository = oauthRepository;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(oauthRepository);
    }
}
