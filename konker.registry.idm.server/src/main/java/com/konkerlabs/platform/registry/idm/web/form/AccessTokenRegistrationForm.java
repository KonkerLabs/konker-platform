package com.konkerlabs.platform.registry.idm.web.form;

import com.konkerlabs.platform.registry.idm.domain.repository.AccessToken;
import com.konkerlabs.platform.registry.idm.web.form.api.ModelBuilder;
import lombok.Data;

import java.util.Date;

@Data
public class AccessTokenRegistrationForm
        implements ModelBuilder<AccessToken, AccessTokenRegistrationForm,Void> {


    private String clientId;
    private String token;
    private Date createTime;

    @Override
    public AccessToken toModel() {
        return AccessToken.builder()
                .clientId(getClientId())
                .token(getToken().getBytes())
                .createTime(getCreateTime())
                .build();
    }

    @Override
    public AccessTokenRegistrationForm fillFrom(AccessToken model) {
        this.setClientId(model.getClientId());
        this.setCreateTime(model.getCreateTime());
        this.setToken(new String(model.getToken()));
        return this;
    }
}
