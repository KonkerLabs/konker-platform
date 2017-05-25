package com.konkerlabs.platform.registry.idm.config;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.idm.domain.repository.AuthorizationCodeRepository;
import com.konkerlabs.platform.registry.idm.domain.repository.OauthClientDetailRepository;
import com.konkerlabs.platform.registry.idm.domain.repository.OauthClientDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service("oauth2ClientDetails")
public class OAuthClientDetailsService implements ClientDetailsService {

    enum Validations {
        INVALID_TENTANT("service.oauth.validation.tenant.invalid"),
        INVALID_ID("service.oauth.validation.id.invalid");

        private String code;

        Validations(String code) {
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

    public static final Integer TOKEN_VALIDITY = 3600 * 365;

    @Autowired
    private OauthClientDetailRepository oauthClientDetailRepository;
    @Autowired
    private AuthorizationCodeRepository authorizationCodeRepository;

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        if (!Optional.ofNullable(clientId).isPresent()) {
            throw new ClientRegistrationException(Validations.INVALID_ID.getCode());
        }
        return oauthClientDetailRepository
                .findOne(clientId)
                .toClientDetails();

    }

    public ServiceResponse<List<OauthClientDetails>> loadClientsByTenant(
            Tenant tenant, Application application) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<List<OauthClientDetails>>error()
                    .withMessage(Validations.INVALID_TENTANT.getCode()).build();
        }
        return ServiceResponseBuilder.<List<OauthClientDetails>>ok()
                .withResult(
                        oauthClientDetailRepository
                                .findAllOauthClientDetailsByTenant(tenant.getId(), application.getName()))
                .build();
    }

    public ServiceResponse<OauthClientDetails> remove(String clientId, Tenant tenant) {
        if (!Optional.ofNullable(clientId).isPresent()) {
            return ServiceResponseBuilder.<OauthClientDetails>error()
                    .withMessage(Validations.INVALID_ID.getCode()).build();
        }
        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<OauthClientDetails>error()
                    .withMessage(Validations.INVALID_TENTANT.getCode()).build();
        }
        oauthClientDetailRepository.delete(
                OauthClientDetails.builder()
                        .clientId(clientId)
                        .tenant(tenant)
                        .build());
        return ServiceResponseBuilder.<OauthClientDetails>ok().build();
    }

    public ServiceResponse<OauthClientDetails> saveClient(Tenant tenant, Application application) {
        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<OauthClientDetails>error()
                    .withMessage(Validations.INVALID_TENTANT.getCode()).build();
        }

        oauthClientDetailRepository.save(
                OauthClientDetails.builder()
                        .tenant(tenant)
                        .clientId(UUID.randomUUID().toString())
                        .accessTokenValidity(TOKEN_VALIDITY)
                        .application(application)
                        .build());

        return ServiceResponseBuilder.<OauthClientDetails>ok().build();
    }

}
