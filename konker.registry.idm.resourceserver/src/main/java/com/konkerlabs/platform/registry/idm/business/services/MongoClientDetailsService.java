package com.konkerlabs.platform.registry.idm.business.services;

import com.konkerlabs.platform.registry.idm.business.repositories.OauthClientDetailRepository;
import com.konkerlabs.platform.registry.idm.business.model.OauthClientDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.util.Assert;

public class MongoClientDetailsService implements ClientDetailsService, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(MongoClientDetailsService.class);

    private OauthClientDetailRepository oauthRepository;

    public MongoClientDetailsService() {
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        final OauthClientDetails oauthClientDetails = oauthRepository
                .findOne(clientId);
        if (oauthClientDetails == null) {
            LOG.warn("Not found ClientDetails by clientId '{}', because null or archived", clientId);
            throw new ClientRegistrationException("Not found ClientDetails by clientId '" + clientId + "', because null or archived");
        }
        return oauthClientDetails.toClientDetails();
    }

    public void setOauthRepository(OauthClientDetailRepository oauthRepository) {
        this.oauthRepository = oauthRepository;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(oauthRepository);
    }

}
