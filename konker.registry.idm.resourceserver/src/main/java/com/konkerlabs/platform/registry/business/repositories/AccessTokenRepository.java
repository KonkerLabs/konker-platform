package com.konkerlabs.platform.registry.business.repositories;

import com.konkerlabs.platform.registry.business.model.AccessToken;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/*@Repository*/
public interface AccessTokenRepository extends MongoRepository<AccessToken, String> {

    @DeleteQuery("{'refreshToken': ?0}")
    void removeAccessTokenByRefreshToken(String refreshToken);

    @Query("{'refreshToken': ?0}")
    AccessToken findAccessTokenByRefreshToken(String refreshToken);

    @Query("{'authenticationId': ?0}")
    AccessToken findAccessTokenByAuthenticationId(String authenticationId);

    @Query("{'username': ?0}")
    List<AccessToken> findAccessTokensByUsername(String userName);

    @Query("{'clientId': ?0}")
    List<AccessToken> findAccessTokensByClientId(String clientId);

    @Query("{'clientId': ?0, 'username': ?1}")
    List<AccessToken> findAccessTokensByClientIdAndUsername(String clientId, String userName);
}