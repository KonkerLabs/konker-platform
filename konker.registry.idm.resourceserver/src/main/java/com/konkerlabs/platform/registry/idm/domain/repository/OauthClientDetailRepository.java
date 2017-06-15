package com.konkerlabs.platform.registry.idm.domain.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;


public interface OauthClientDetailRepository extends MongoRepository<OauthClientDetails, String> {

    /*@Query("{ 'clientId' : ?0 }")
    OauthClientDetails findOauthClientDetails(String clientId);*/

    @Query("{'tenant.id': ?0, 'application.name': ?1}")
    List<OauthClientDetails> findAllOauthClientDetailsByTenant(String tenantId, String applicationId);

    @Query("'application.name': ?0}")
    List<OauthClientDetails> findAllOauthClientDetailsByApplication(String applicationId);

    /*boolean updateOauthClientDetailsArchive(String clientId, boolean archive);*/


    /*void saveOauthClientDetails(OauthClientDetails clientDetails);

    boolean removeOauthClientDetails(OauthClientDetails clientDetails);*/

    /*void saveAuthorizationCode(AuthorizationCode authorizationCode);

    AuthorizationCode removeAuthorizationCode(String code);*/
}