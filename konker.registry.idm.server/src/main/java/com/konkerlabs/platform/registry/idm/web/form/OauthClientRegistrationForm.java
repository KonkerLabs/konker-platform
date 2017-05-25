package com.konkerlabs.platform.registry.idm.web.form;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.idm.domain.repository.OauthClientDetails;
import com.konkerlabs.platform.registry.idm.web.form.api.ModelBuilder;
import lombok.Data;

@Data
public class OauthClientRegistrationForm
        implements ModelBuilder<OauthClientDetails, OauthClientRegistrationForm,Void> {


    private String clientId;
    private String name;
    private Application application;
    private String clientSecret;
    private Tenant tenant;

    @Override
    public OauthClientDetails toModel() {
        return OauthClientDetails.builder()
                .clientId(getClientId())
                .application(getApplication())
                .tenant(getTenant())
                .build();
    }

    @Override
    public OauthClientRegistrationForm fillFrom(OauthClientDetails model) {
        this.setApplication(model.getApplication());
        this.setName(model.getName());
        this.setClientId(model.getClientId());
        this.setClientSecret(model.getClientSecret());
        return this;
    }
}
