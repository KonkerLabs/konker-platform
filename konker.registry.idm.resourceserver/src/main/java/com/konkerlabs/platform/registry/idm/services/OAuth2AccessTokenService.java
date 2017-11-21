package com.konkerlabs.platform.registry.idm.services;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.repositories.RoleRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class OAuth2AccessTokenService {

    @Autowired
    private DefaultTokenServices defaultTokenServices;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OAuthClientDetailsService oauthClientDetailsService;

    public ServiceResponse<OAuth2AccessToken> getGatewayAccessToken(Tenant tenant, Application application, Gateway gateway) {

        // get gateway oauth client
        ServiceResponse<OauthClientDetails> clientDetailsResponse = getGatewayClient(tenant, application, gateway);
        if (!clientDetailsResponse.isOk()) {
            return ServiceResponseBuilder.<OAuth2AccessToken>error()
                    .withMessages(clientDetailsResponse.getResponseMessages())
                    .build();
        }

        OauthClientDetails clientDetails = clientDetailsResponse.getResult();

        Role gatewayRole = roleRepository.findByName("ROLE_IOT_USER");
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Privilege privilege : gatewayRole.getPrivileges()) {
            authorities.add(new SimpleGrantedAuthority(privilege.getName()));
        }

        Set<String> scopes = new HashSet<>();
        scopes.add("read");
        scopes.add("write");

        OAuth2Request authorizationRequest = new OAuth2Request(
                null, clientDetails.getClientId(),
                authorities, true, scopes, null, "",
                null, null);

        OAuth2Authentication authenticationRequest = new OAuth2Authentication(
                authorizationRequest, null);
        authenticationRequest.setAuthenticated(true);

        OAuth2AccessToken accessToken = defaultTokenServices.createAccessToken(authenticationRequest);

        return ServiceResponseBuilder.<OAuth2AccessToken>ok()
                .withResult(accessToken)
                .build();

    }

    private ServiceResponse<OauthClientDetails> getGatewayClient(Tenant tenant, Application application, Gateway gateway) {
        OauthClientDetails clientDetails;

        ServiceResponse<OauthClientDetails> oauthClientResponse = oauthClientDetailsService.loadClientByIdAsRoot(gateway.getRoutUriTemplate());

        if (!oauthClientResponse.isOk()) {
            // check if the response is of 'not found'
            if (oauthClientResponse.getResponseMessages().containsKey(OAuthClientDetailsService.Messages.CLIENT_CREDENTIALS_INVALID.getCode())) {
                // if not exists, creoauthClientDetailRepositoryate a new one
                clientDetails = OauthClientDetails.builder()
                        .build()
                        .setGatewayProperties(gateway);

                return oauthClientDetailsService.saveClient(tenant, application, clientDetails);
            } else {
                return oauthClientResponse;
            }
        } else {
            return oauthClientResponse;
        }

    }

}
