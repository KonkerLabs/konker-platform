package com.konkerlabs.platform.registry.idm.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.konkerlabs.platform.registry.business.model.AccessToken;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.OauthClientDetails;
import com.konkerlabs.platform.registry.business.model.Role;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.repositories.AccessTokenRepository;
import com.konkerlabs.platform.registry.business.repositories.AuthorizationCodeRepository;
import com.konkerlabs.platform.registry.business.repositories.OauthClientDetailRepository;
import com.konkerlabs.platform.registry.business.repositories.RoleRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.business.services.api.RoleService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;

@Service("oauth2ClientDetails")
public class OAuthClientDetailsService implements ClientDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthClientDetailsService.class);

    public enum Validations {
        INVALID_TENANT("service.oauth.validation.tenant.invalid"),
        INVALID_ID("service.oauth.validation.id.invalid"),
        INVALID_DETAILS("service.oauth.validation.details.invalid");

        private String code;

        Validations(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public enum Messages {
        CLIENT_REGISTERED_SUCCESSFULLY("controller.clients.registered.success"),
        CLIENT_REMOVED_SUCCESSFULLY("controller.clients.removed.succesfully"),
        CLIENT_REMOVED_UNSUCCESSFULLY("controller.clients.removed.unsuccesfully"),
        TOKEN_REMOVED_SUCCESSFULLY("controller.tokens.removed.succesfully"),
        TOKEN_REMOVED_UNSUCCESSFULLY("controller.tokens.removed.unsuccesfully"),
        CLIENT_CREDENTIALS_INVALID("controller.clients.credentials.invalid");

        private String code;

        Messages(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    enum Errors {
        ERROR_SAVE_USER("service.oauth.error.saveClient");

        private String code;

        Errors(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

    }

    @Autowired
    private OauthClientDetailRepository oauthClientDetailRepository;
    @Autowired
    private AccessTokenRepository accessTokenRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;

    public boolean validatePassword(String raw, String encoded) {
        return slowEquals(raw.getBytes(), encoded.getBytes());
    }

    private boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++)
            diff |= a[i] ^ b[i];
        return diff == 0;
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        if (!Optional.ofNullable(clientId).isPresent()) {
            throw new ClientRegistrationException(Validations.INVALID_ID.getCode());
        }
        ServiceResponse<OauthClientDetails> response = loadClientByIdAsRoot(clientId);
        if (!Optional.ofNullable(response).isPresent() || !response.isOk()) {
            throw new ClientRegistrationException("Invalid credentials");
        }

        return response.getResult().toClientDetails();

    }

    public ServiceResponse<OauthClientDetails> loadClientById(Tenant tenant, String clientId) throws ClientRegistrationException {
        if (!Optional.ofNullable(clientId).isPresent()) {
            throw new ClientRegistrationException(Validations.INVALID_ID.getCode());
        }
        OauthClientDetails details = oauthClientDetailRepository.findOne(clientId);
        if (details != null) {
            if (!details.getTenant().getId().equals(tenant.getId())) {
                return ServiceResponseBuilder.<OauthClientDetails>error()
                        .withMessage(Messages.CLIENT_CREDENTIALS_INVALID.getCode())
                        .build();
            }
            return ServiceResponseBuilder.<OauthClientDetails>ok()
                    .withResult(details)
                    .build();
        } else {
            return ServiceResponseBuilder.<OauthClientDetails>error()
                    .withMessage(Messages.CLIENT_CREDENTIALS_INVALID.getCode())
                    .build();
        }

    }

    public ServiceResponse<OauthClientDetails> loadClientByIdAsRoot(String clientId) throws ClientRegistrationException {
        if (!Optional.ofNullable(clientId).isPresent()) {
            throw new ClientRegistrationException(Validations.INVALID_ID.getCode());
        }
        OauthClientDetails details = oauthClientDetailRepository.findOne(clientId);
        if (details != null) {
            return ServiceResponseBuilder.<OauthClientDetails>ok()
                    .withResult(details)
                    .build();
        } else {
            User user = userRepository.findByEmail(clientId);
            if (user != null) {
                return ServiceResponseBuilder.<OauthClientDetails>ok()
                        .withResult(OauthClientDetails.builder().build().setUserProperties(user))
                        .build();
            }

            return ServiceResponseBuilder.<OauthClientDetails>error()
                    .withMessage(Messages.CLIENT_CREDENTIALS_INVALID.getCode())
                    .build();
        }
    }

    public ServiceResponse<AccessToken> loadTokenByIdAsRoot(String tokenId) {

        AccessToken details = accessTokenRepository.findOne(tokenId);
        if (details != null) {
            return ServiceResponseBuilder.<AccessToken>ok()
                    .withResult(details)
                    .build();
        } else {
            return ServiceResponseBuilder.<AccessToken>error()
                    .withMessage(Messages.CLIENT_CREDENTIALS_INVALID.getCode())
                    .build();
        }
    }

    public ServiceResponse<AccessToken> loadTokenById(Tenant tenant, String tokenId) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<AccessToken>error()
                    .withMessage(Validations.INVALID_TENANT.getCode()).build();
        }

        if (!Optional.ofNullable(tokenId).isPresent()) {
            return ServiceResponseBuilder.<AccessToken>error()
                    .withMessage(Validations.INVALID_DETAILS.getCode()).build();
        }

        AccessToken details = accessTokenRepository.findOne(tokenId);
        if (details != null) {
            ServiceResponse<OauthClientDetails> clientDetails =
                    loadClientById(tenant, tokenId);

            if (Optional.ofNullable(clientDetails).isPresent()
                    || Optional.of(clientDetails.getResult()).isPresent()) {
                return ServiceResponseBuilder.<AccessToken>error()
                        .withMessage(Messages.CLIENT_CREDENTIALS_INVALID.getCode())
                        .build();
            }

            return ServiceResponseBuilder.<AccessToken>ok()
                    .withResult(details)
                    .build();
        } else {
            return ServiceResponseBuilder.<AccessToken>error()
                    .withMessage(Messages.CLIENT_CREDENTIALS_INVALID.getCode())
                    .build();
        }
    }

    public ServiceResponse<AccessToken> deleteTokenAsRoot(String id) {

        if (StringUtils.isEmpty(id)) {
            return ServiceResponseBuilder.<AccessToken>error()
                    .withMessage(Validations.INVALID_DETAILS.getCode()).build();
        }
        AccessToken fromDB = accessTokenRepository.findOne(id);

        if (!Optional.ofNullable(fromDB).isPresent()) {
            return ServiceResponseBuilder.<AccessToken>error()
                    .withMessage(Validations.INVALID_DETAILS.getCode()).build();
        }

        accessTokenRepository.delete(id);

        return ServiceResponseBuilder.<AccessToken>ok().build();
    }

    public ServiceResponse<AccessToken> deleteToken(Tenant tenant, String id) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<AccessToken>error()
                    .withMessage(Validations.INVALID_TENANT.getCode()).build();
        }

        if (StringUtils.isEmpty(id)) {
            return ServiceResponseBuilder.<AccessToken>error()
                    .withMessage(Validations.INVALID_DETAILS.getCode()).build();
        }
        AccessToken fromDB = accessTokenRepository.findOne(id);

        if (!Optional.ofNullable(fromDB).isPresent()) {
            return ServiceResponseBuilder.<AccessToken>error()
                    .withMessage(Validations.INVALID_DETAILS.getCode()).build();
        }
        OauthClientDetails oauthClientDetails =
                oauthClientDetailRepository.findOne(fromDB.getClientId());

        if (!Optional.ofNullable(oauthClientDetails).isPresent()) {
            return ServiceResponseBuilder.<AccessToken>error()
                    .withMessage(Validations.INVALID_DETAILS.getCode()).build();
        }

        if (!oauthClientDetails.getTenant().getId().equals(tenant.getId())) {
            return ServiceResponseBuilder.<AccessToken>error()
                    .withMessage(Validations.INVALID_DETAILS.getCode()).build();
        }

        accessTokenRepository.delete(id);

        return ServiceResponseBuilder.<AccessToken>ok().build();
    }

    public ServiceResponse<List<AccessToken>> loadAllTokens(
            Application application) {

        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<List<AccessToken>>error()
                    .withMessage(Validations.INVALID_DETAILS.getCode()).build();
        }

        ServiceResponse<List<OauthClientDetails>> clientsByApplication =
                loadAllClients(application);

        List<AccessToken> tokens = new ArrayList<>();
        if (clientsByApplication.isOk()) {
            clientsByApplication.getResult().stream().forEach(client -> {
                tokens.addAll(accessTokenRepository.findAccessTokensByClientId(
                        client.getClientId()
                ));
            });
        }

        return ServiceResponseBuilder.<List<AccessToken>>ok()
                .withResult(tokens).build();
    }

    public ServiceResponse<List<AccessToken>> loadTokensByTenant(
            Tenant tenant, Application application) {

        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<List<AccessToken>>error()
                    .withMessage(Validations.INVALID_DETAILS.getCode()).build();
        }

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<List<AccessToken>>error()
                    .withMessage(Validations.INVALID_TENANT.getCode()).build();
        }

        ServiceResponse<List<OauthClientDetails>> clientsByApplicationAndTenant =
                loadClientsByTenant(tenant, application);

        List<AccessToken> tokens = new ArrayList<>();
        if (clientsByApplicationAndTenant.isOk()) {
            clientsByApplicationAndTenant.getResult().stream().forEach(client -> {
                tokens.addAll(accessTokenRepository.findAccessTokensByClientId(
                        client.getClientId()
                ));
            });
        }

        return ServiceResponseBuilder.<List<AccessToken>>ok()
                .withResult(tokens).build();
    }

    public ServiceResponse<List<OauthClientDetails>> loadAllClients(
            Application application) {

        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<List<OauthClientDetails>>error()
                    .withMessage(Validations.INVALID_DETAILS.getCode()).build();
        }
        return ServiceResponseBuilder.<List<OauthClientDetails>>ok()
                .withResult(
                        oauthClientDetailRepository
                                .findAllOauthClientDetailsByApplication(application.getName()))
                .build();
    }

    public ServiceResponse<List<OauthClientDetails>> loadClientsByTenant(
            Tenant tenant, Application application) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<List<OauthClientDetails>>error()
                    .withMessage(Validations.INVALID_TENANT.getCode()).build();
        }
        return ServiceResponseBuilder.<List<OauthClientDetails>>ok()
                .withResult(
                        oauthClientDetailRepository
                                .findAllOauthClientDetailsByTenant(tenant.getId(), application.getName()))
                .build();
    }

    public ServiceResponse<OauthClientDetails> deleteClientAsRoot(String clientId) {
        if (!Optional.ofNullable(clientId).isPresent()) {
            return ServiceResponseBuilder.<OauthClientDetails>error()
                    .withMessage(Validations.INVALID_ID.getCode()).build();
        }
        oauthClientDetailRepository.delete(
                OauthClientDetails.builder()
                        .clientId(clientId)
                        .build());
        return ServiceResponseBuilder.<OauthClientDetails>ok().build();
    }

    public ServiceResponse<OauthClientDetails> deleteClient(Tenant tenant, String clientId) {
        if (!Optional.ofNullable(clientId).isPresent()) {
            return ServiceResponseBuilder.<OauthClientDetails>error()
                    .withMessage(Validations.INVALID_ID.getCode()).build();
        }
        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<OauthClientDetails>error()
                    .withMessage(Validations.INVALID_TENANT.getCode()).build();
        }
        oauthClientDetailRepository.delete(
                OauthClientDetails.builder()
                        .clientId(clientId)
                        .tenant(tenant)
                        .build());
        return ServiceResponseBuilder.<OauthClientDetails>ok().build();
    }

    public ServiceResponse<OauthClientDetails> saveClientAsRoot(
            String tenantDomainName, Application application, OauthClientDetails clientDetails) {
        Tenant tenant = tenantRepository.findByDomainName(tenantDomainName);
        if (!Optional.ofNullable(tenant).isPresent()) {
            if (!Optional.ofNullable(clientDetails).isPresent()) {
                return ServiceResponseBuilder.<OauthClientDetails>error()
                        .withMessage(Validations.INVALID_DETAILS.getCode()).build();
            }
        }

        return saveClient(tenant, application, clientDetails);
    }

    private List<Role> getClientRoles() {
        return Collections.singletonList(roleRepository.findByName(RoleService.ROLE_IOT_USER));
    }

    public ServiceResponse<OauthClientDetails> saveClient(Tenant tenant, Application application, OauthClientDetails clientDetails) {
        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<OauthClientDetails>error()
                    .withMessage(Validations.INVALID_TENANT.getCode()).build();
        }

        if (!Optional.ofNullable(clientDetails).isPresent()) {
            return ServiceResponseBuilder.<OauthClientDetails>error()
                    .withMessage(Validations.INVALID_DETAILS.getCode()).build();
        }

        if (!Optional.ofNullable(clientDetails.getClientId()).isPresent()) {
            return ServiceResponseBuilder.<OauthClientDetails>error()
                    .withMessage(Validations.INVALID_DETAILS.getCode()).build();
        }

        if (!Optional.ofNullable(clientDetails.getClientSecret()).isPresent()) {
            clientDetails.setClientSecret(UUID.randomUUID().toString());
        }

        clientDetails.setRoles(getClientRoles());

        oauthClientDetailRepository.save(
                OauthClientDetails.builder()
                        .clientId(clientDetails.getClientId())
                        .parentUser(clientDetails.getParentUser())
                        .parentGateway(clientDetails.getParentGateway())
                        .tenant(tenant)
                        .clientSecret(clientDetails.getClientSecret())
                        .accessTokenValidity(clientDetails.getAccessTokenValidity())
                        .application(application)
                        .webServerRedirectUri(clientDetails.getWebServerRedirectUri())
                        .roles(clientDetails.getRoles())
                        .build());

        return ServiceResponseBuilder.<OauthClientDetails>ok()
                .withResult(clientDetails)
                .build();
    }

}
