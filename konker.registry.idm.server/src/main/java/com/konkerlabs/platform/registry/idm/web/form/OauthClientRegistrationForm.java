package com.konkerlabs.platform.registry.idm.web.form;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.idm.domain.repository.OauthClientDetails;
import com.konkerlabs.platform.registry.idm.web.form.api.ModelBuilder;
import lombok.Data;

import java.util.Optional;
import java.util.UUID;

@Data
public class OauthClientRegistrationForm
        implements ModelBuilder<OauthClientDetails, OauthClientRegistrationForm,Void> {


    private String clientId;
    private String name;
    private Application application;
    private String clientSecret;
    private Tenant tenant;
    private Boolean active;
    private String webServerRedirectUri;

    @Override
    public OauthClientDetails toModel() {
        return OauthClientDetails.builder()
                .clientId(getClientId())
                .application(getApplication())
                .tenant(getTenant())
                .active(Optional.ofNullable(getActive()).isPresent() ? getActive() : true)
                .clientSecret(Optional.ofNullable(
                        getClientSecret()).isPresent() ?
                        getClientSecret() : UUID.randomUUID().toString())
                .webServerRedirectUri(getWebServerRedirectUri())
                .build();
    }

    @Override
    public OauthClientRegistrationForm fillFrom(OauthClientDetails model) {
        this.setApplication(model.getApplication());
        this.setClientId(model.getClientId());
        this.setClientSecret(model.getClientSecret());
        this.setActive(model.getActive());
        this.setWebServerRedirectUri(model.getWebServerRedirectUri());

        return this;
    }
}
