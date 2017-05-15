package com.konkerlabs.platform.registry.idm.oauth.mongo.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessTokenRepository {

    void saveAccessToken(AccessToken accessToken);

    AccessToken findAccessToken(String tokenId);

    void removeAccessToken(String tokenId);

    void saveRefreshToken(RefreshToken refreshToken);

    RefreshToken findRefreshToken(String tokenId);

    void removeRefreshToken(String tokenId);

    void removeAccessTokenByRefreshToken(String refreshToken);

    AccessToken findAccessTokenByRefreshToken(String refreshToken);

    AccessToken findAccessTokenByAuthenticationId(String authenticationId);

    List<AccessToken> findAccessTokensByUsername(String userName);

    List<AccessToken> findAccessTokensByClientId(String clientId);

    List<AccessToken> findAccessTokensByClientIdAndUsername(String clientId, String userName);
}