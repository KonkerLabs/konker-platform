package com.konkerlabs.platform.registry.web.forms;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import com.konkerlabs.platform.registry.web.forms.api.ModelBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;
import java.util.function.Supplier;

@Data
@Builder
public class UserForm implements ModelBuilder<User,UserForm,Void> {

    private String email;
    private String name;
    private String phone;
    private String oldPassword;
    private String newPassword;
    private String newPasswordConfirmation;
    private Language language;
    private TimeZone zoneId;
    private String avatar;
    private DateFormat dateFormat;
    private Tenant tenant;


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
                .tenant(tenant)
                .build();
    }

    @Override
    public UserForm fillFrom(User model) {
        this.setName(model.getName());
        this.setEmail(model.getEmail());
        this.setPhone(model.getPhone());
        this.setAvatar(model.getAvatar());
        this.setDateFormat(model.getDateFormat());
        this.setLanguage(model.getLanguage());
        this.setZoneId(model.getZoneId());
        this.setEmail(model.getEmail());
        this.setTenant(model.getTenant());
        return this;
    }

    @Override
    public void setAdditionalSupplier(Supplier<Void> voidSupplier) {}

}
