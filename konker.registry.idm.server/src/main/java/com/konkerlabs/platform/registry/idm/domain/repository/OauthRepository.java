package com.konkerlabs.platform.registry.idm.domain.repository;



import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OauthRepository {

    OauthClientDetails findOauthClientDetails(String clientId);

    List<OauthClientDetails> findAllOauthClientDetails();

    boolean updateOauthClientDetailsArchive(String clientId, boolean archive);

    void saveOauthClientDetails(OauthClientDetails clientDetails);

    boolean removeOauthClientDetails(OauthClientDetails clientDetails);

    void saveAuthorizationCode(AuthorizationCode authorizationCode);

    AuthorizationCode removeAuthorizationCode(String code);
}