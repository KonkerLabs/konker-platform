package com.konkerlabs.platform.registry.api.web.controller;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.model.UserSubscriptionVO;
import com.konkerlabs.platform.registry.api.model.UserVO;
import com.konkerlabs.platform.registry.business.model.Role;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import com.konkerlabs.platform.registry.business.services.api.RoleService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.business.services.api.UserService.Errors;
import com.konkerlabs.platform.registry.business.services.api.UserService.Validations;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.*;

@RestController
@Scope("request")
@RequestMapping(
        value = "/userSubscription"
)
@Api(tags = "user subscription")
public class UserSubscriptionRestController implements InitializingBean {

	@Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    private Role role;

    private Set<String> validationsCode = new HashSet<>();

    @PostMapping
    @ApiOperation(value = "Request a new user access to platform")
    public UserVO create(
            @ApiParam(name = "body", value = "JSON filled with the fields described in Model and Example Value beside", required = true)
            @RequestBody UserSubscriptionVO userForm) throws BadServiceResponseException {

        Role role = roleService.findByName(RoleService.ROLE_IOT_USER).getResult();

        PasswordType passwordType = getPasswordType(userForm.getPasswordType());
        if (passwordType == null) {
            Map<String, Object[]> responseMessages = new HashMap<>();
            responseMessages.put(Validations.INVALID_PASSWORD_TYPE.getCode(), null);

            throw new BadServiceResponseException(responseMessages, validationsCode);
        }

        User.JobEnum job = getJob(userForm.getJobTitle());

        Tenant tenant = Tenant
                            .builder()
                            .name(userForm.getCompany())
                            .build();

        User userFromForm = User.builder()
                .email(userForm.getEmail())
                .tenant(tenant)
                .password(userForm.getPassword())
                .phone(userForm.getPhoneNumber())
                .name(userForm.getName())
                .job(job)
                .notificationViaEmail(true)
                .dateFormat(DateFormat.YYYYMMDD)
                .zoneId(TimeZone.AMERICA_SAO_PAULO)
                .language(Language.PT_BR)
                .roles(Collections.singletonList(role))
                .registrationDate(Instant.now())
                .build();

        ServiceResponse<User> userResponse = null;

        switch (passwordType) {
            case PASSWORD:
                String password = null;

                if (Optional.ofNullable(userForm.getPassword()).isPresent() && !userForm.getPassword().isEmpty()) {
                    password = userForm.getPassword();
                }

                userResponse = userService.createAccount(userFromForm, password, password);
                break;
            case BCRYPT_HASH:
                String bcryptHash = String.format("Bcrypt%s", userForm.getPassword());
                userResponse = userService.createAccountWithPasswordHash(userFromForm, bcryptHash);
                break;
            case PBKDF2_HASH:
                String pbkdf2Hash = String.format("PBKDF2WithHmac%s", userForm.getPassword());
                userResponse = userService.createAccountWithPasswordHash(userFromForm, pbkdf2Hash);
                break;
        }

        if (!userResponse.isOk()) {
            throw new BadServiceResponseException(userResponse, validationsCode);
        } else {
            return new UserVO().apply(userResponse.getResult());
        }

    }

    private User.JobEnum getJob(String jobTitle) {
        for (User.JobEnum type: User.JobEnum.values()) {
            if (type.name().equals(jobTitle)) {
                return type;
            }
        }

        return null;
    }

    private PasswordType getPasswordType(String passwordType) {
        for (PasswordType type: PasswordType.values()) {
            if (type.name().equals(passwordType)) {
                return type;
            }
        }

        return null;
    }

    @Override
    public void afterPropertiesSet() {

        for (Validations value : Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (Errors value : Errors.values()) {
            validationsCode.add(value.getCode());
        }

    }

    private enum PasswordType {

        PASSWORD,
        BCRYPT_HASH,
        PBKDF2_HASH

    }

}
