package com.konkerlabs.platform.registry.web.forms;

import java.util.function.Supplier;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.User.JobEnum;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import com.konkerlabs.platform.registry.config.CdnConfig;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data
@Builder
public class UserForm implements ModelBuilder<User,UserForm,Void> {

    private String email;
    private String name;
    private String phone;
    private String oldPassword;
    private String newPassword;
    private String newPasswordConfirmation;
    private String token;
    private Language language;
    private TimeZone zoneId;
    private String avatar;
    private DateFormat dateFormat;
    private LogLevel logLevel;
    private Tenant tenant;
    private String tenantName;
    private Boolean avatarUploadEnabled = Boolean.FALSE;
    private boolean notificationViaEmail;
    private boolean acceptedTerms;
    private JobEnum job;



    @Tolerate
    public UserForm() {}

    @Override
    public User toModel() {
        return User.builder()
                .name(getName())
                .email(getEmail())
                .dateFormat(getDateFormat())
                .language(getLanguage())
                .avatar(getAvatar())
                .zoneId(getZoneId())
                .phone(getPhone())
                .tenant(getTenant())
                .notificationViaEmail(isNotificationViaEmail())
                .acceptedTerms(isAcceptedTerms())
                .job(job)
                .build();
    }

    @Override
    public UserForm fillFrom(User model) {
    	CdnConfig cdnConfig = new CdnConfig();

    	this.setName(model.getName());
        this.setEmail(model.getEmail());
        this.setPhone(model.getPhone());
        this.setDateFormat(model.getDateFormat());
        this.setLanguage(model.getLanguage());
        this.setZoneId(model.getZoneId());
        this.setEmail(model.getEmail());
        this.setTenant(model.getTenant());
        this.setLogLevel(model.getTenant().getLogLevel());
        this.setAvatarUploadEnabled(cdnConfig.isEnabled());
        this.setNotificationViaEmail(model.isNotificationViaEmail());
        this.setJob(model.getJob());
        return this;
    }

    @Override
    public void setAdditionalSupplier(Supplier<Void> voidSupplier) {}

}
