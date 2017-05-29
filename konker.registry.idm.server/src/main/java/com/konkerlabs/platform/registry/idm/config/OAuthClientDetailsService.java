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
        CLIENT_REMOVED_UNSUCCESSFULLY("controller.clients.removed.unsuccesfully");

        private String code;

        Messages(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    static enum Errors {
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
        ServiceResponse<OauthClientDetails> response =
                loadById(clientId);
        if(Optional.ofNullable(response).isPresent() && response.isOk()){
            return response.getResult().toClientDetails();
        }
        return null;

    }

    public ServiceResponse<OauthClientDetails> loadById(String clientId) throws ClientRegistrationException {
        if (!Optional.ofNullable(clientId).isPresent()) {
            throw new ClientRegistrationException(Validations.INVALID_ID.getCode());
        }
        OauthClientDetails details = oauthClientDetailRepository.findOne(clientId);
        if(details != null){
            return ServiceResponseBuilder.<OauthClientDetails> ok()
                    .withResult(details)
                    .build();
        } else {
            return ServiceResponseBuilder.<OauthClientDetails> ok()
                    .build();
        }



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

    public ServiceResponse<OauthClientDetails> saveClient(Tenant tenant, Application application, OauthClientDetails clientDetails) {
        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<OauthClientDetails>error()
                    .withMessage(Validations.INVALID_TENTANT.getCode()).build();
        }

        if(!Optional.ofNullable(clientDetails).isPresent()){
            return ServiceResponseBuilder.<OauthClientDetails>error()
                    .withMessage(Validations.INVALID_DETAILS.getCode()).build();
        }

        if(!Optional.ofNullable(clientDetails.getClientId()).isPresent()){
            return ServiceResponseBuilder.<OauthClientDetails>error()
                    .withMessage(Validations.INVALID_DETAILS.getCode()).build();
        }

        if(!Optional.ofNullable(clientDetails.getWebServerRedirectUri()).isPresent()){
            return ServiceResponseBuilder.<OauthClientDetails>error()
                    .withMessage(Validations.INVALID_DETAILS.getCode()).build();
        }

        if(!Optional.ofNullable(clientDetails.getClientId()).isPresent()){
            return ServiceResponseBuilder.<OauthClientDetails>error()
                    .withMessage(Validations.INVALID_DETAILS.getCode()).build();
        }
        if(!Optional.ofNullable(clientDetails.getClientSecret()).isPresent()){
            clientDetails.setClientSecret(UUID.randomUUID().toString());
        }


        oauthClientDetailRepository.save(
                OauthClientDetails.builder()
                        .clientId(clientDetails.getClientId())
                        .tenant(tenant)
                        .clientSecret(clientDetails.getClientSecret())
                        .accessTokenValidity(TOKEN_VALIDITY)
                        .application(application)
                        .webServerRedirectUri(clientDetails.getWebServerRedirectUri())
                        .build());

        return ServiceResponseBuilder.<OauthClientDetails>ok().build();
    }

}
