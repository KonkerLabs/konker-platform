package com.konkerlabs.platform.registry.business.repositories;


import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.OauthClientDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;


public interface OauthClientDetailRepository extends MongoRepository<OauthClientDetails, String> {

    @Query("{'tenant.id': ?0, 'application.name': ?1}")
    List<OauthClientDetails> findAllOauthClientDetailsByTenant(String tenantId, String applicationId);

    @Query("'application.name': ?0}")
    List<OauthClientDetails> findAllOauthClientDetailsByApplication(String applicationId);

    @Query("{ 'application.name' : ?0, 'clientSecret' : ?1 }")
    OauthClientDetails findByApplicationAndSecret(String applicationName, String clientSecret);

}