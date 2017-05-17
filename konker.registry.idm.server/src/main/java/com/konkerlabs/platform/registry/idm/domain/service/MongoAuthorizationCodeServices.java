
package com.konkerlabs.platform.registry.idm.domain.service;


import com.konkerlabs.platform.registry.idm.domain.repository.AuthorizationCode;
import com.konkerlabs.platform.registry.idm.domain.repository.OauthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.RandomValueAuthorizationCodeServices;
import org.springframework.util.Assert;


public class MongoAuthorizationCodeServices extends RandomValueAuthorizationCodeServices implements InitializingBean {


    private static final Logger LOG = LoggerFactory.getLogger(MongoAuthorizationCodeServices.class);


    private OauthRepository oauthRepository;


    public MongoAuthorizationCodeServices() {
    }


    @Override
    protected void store(String code, OAuth2Authentication authentication) {
        AuthorizationCode authorizationCode = new AuthorizationCode()
                .code(code).authentication(authentication);

        oauthRepository.saveAuthorizationCode(authorizationCode);
        LOG.debug("Store AuthorizationCode: {}", authorizationCode);
    }

    @Override
    protected OAuth2Authentication remove(String code) {
        AuthorizationCode authorizationCode = oauthRepository.removeAuthorizationCode(code);
        LOG.debug("Remove AuthorizationCode: {}", authorizationCode);
        return authorizationCode != null ? authorizationCode.authentication() : null;
    }

    public void setOauthRepository(OauthRepository oauthRepository) {
        this.oauthRepository = oauthRepository;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(oauthRepository, "oauthRepository is null");
    }
}
