
package com.konkerlabs.platform.registry.idm.domain.service;


import com.konkerlabs.platform.registry.idm.domain.repository.AuthorizationCode;
import com.konkerlabs.platform.registry.idm.domain.repository.AuthorizationCodeRepository;
import com.konkerlabs.platform.registry.idm.domain.repository.OauthClientDetailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.RandomValueAuthorizationCodeServices;
import org.springframework.util.Assert;


public class MongoAuthorizationCodeServices extends RandomValueAuthorizationCodeServices implements InitializingBean {


    private static final Logger LOG = LoggerFactory.getLogger(MongoAuthorizationCodeServices.class);


    @Autowired
    private OauthClientDetailRepository oauthRepository;

    @Autowired
    private AuthorizationCodeRepository authorizationCodeRepository;


    public MongoAuthorizationCodeServices() {
    }


    @Override
    protected void store(String code, OAuth2Authentication authentication) {
        AuthorizationCode authorizationCode =
                AuthorizationCode.builder()
                        .code(code)
                        .build()
                        .authentication(authentication);

        authorizationCodeRepository.save(authorizationCode);
        LOG.debug("Store AuthorizationCode: {}", authorizationCode);
    }

    @Override
    protected OAuth2Authentication remove(String code) {
        AuthorizationCode authorizationCode =
                authorizationCodeRepository.findOne(code);
        authorizationCodeRepository.delete(authorizationCode);
        LOG.debug("Remove AuthorizationCode: {}", authorizationCode);
        return authorizationCode != null ? authorizationCode.authentication() : null;
    }

    public void setOauthRepository(OauthClientDetailRepository oauthRepository) {
        this.oauthRepository = oauthRepository;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(oauthRepository, "oauthRepository is null");
    }
}
