package com.konkerlabs.platform.registry.business.services.api;


import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;

import java.util.List;

public interface UserService {

    enum Validations {
    	MISSING_PARAMETERS("service.user.validation.missing.parameters"),
        INVALID_USER_DETAILS("service.user.validation.detail.invalid"),
        INVALID_USER_EMAIL("service.user.validation.email.invalid"),
        INVALID_USER_NAME("service.user.validation.name.invalid"),
        INVALID_USER_PREFERENCE_DATEFORMAT("service.user.validation.dateformat.invalid"),
        INVALID_USER_PREFERENCE_LOCALE("service.user.validation.locale.invalid"),
        INVALID_USER_PREFERENCE_LANGUAGE("service.user.validation.language.invalid"),
        INVALID_PASSWORD_LENGTH("service.user.validation.password.invalid.length"),
        INVALID_PASSWORD_USER_DATA("service.user.validation.password.invalid.userdata"),
        INVALID_PASSWORD_CONFIRMATION("service.user.validation.password.invalid.confirmation"),
        INVALID_PASSWORD_INVALID("service.user.validation.password.invalid"),
        INVALID_PASSWORD_HASH_INVALID("service.user.validation.password_hash.invalid"),
        INVALID_PASSWORD_BLACKLISTED("service.user.validation.password.invalid.blacklisted"),
        INVALID_PASSWORD_TYPE("service.user.validation.password.invalid_type"),
        INVALID_AVATAR("service.user.validation.avatar.invalid"),
        INVALID_USER_LIMIT_CREATION("service.user.validation.limit.creation"),
        NO_EXIST_USER("service.user.validation.no.exist"),
        USER_EXIST("service.user.validation.exist"),
        NO_PERMISSION_TO_REMOVE("service.user.validation.no_permission_to_remove_user"),
        NO_PERMISSION_TO_REMOVE_HIMSELF("service.user.validation.no_permission_to_remove_himself");

        private String code;

        Validations(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

    }
    enum Errors {
        ERROR_SAVE_USER("service.user.error.detail.save");

        private String code;

        Errors(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }


    }
    enum Messages {
    	USER_REGISTERED_SUCCESSFULLY("controller.user.registered.success"),
    	USER_ACTIVATED_SUCCESSFULLY("controller.user.activated.success"),
        USER_SUBJECT_MAIL("user.email.subject.activation"),
        USER_HAS_ACCOUNT("user.email.subject.has.account");

        private String code;

        Messages(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }


    }
    /**
     * Save user details
     * @param user
     * @return ServiceResponse<User>
     */
    ServiceResponse<User> save(User user,
                               String password,
                               String newPassword,
                               String newPasswordConfirmation);

    ServiceResponse<User> save(User user,
    		String newPassword,
    		String newPasswordConfirmation);

    ServiceResponse<User> save(String application,
                               String location,
                               User user,
                               String newPassword,
                               String newPasswordConfirmation);

    ServiceResponse<User> createAccount(User user,
    		String newPassword,
            String newPasswordConfirmation);


    ServiceResponse<User> createAccountWithPasswordHash(User user,
    		String passwordHash);

    ServiceResponse<User> findByEmail(String email);

    ServiceResponse<List<User>> findAll(Tenant tenant);

	ServiceResponse<User> findByTenantAndEmail(Tenant tenant, String email);

    ServiceResponse<User> remove(Tenant tenant, User loggedUser, String emailUserToRemove);

    ServiceResponse<List<User>> findAllByApplicationLocation(Tenant tenant, Application application, Location location);

}
