package com.konkerlabs.platform.registry.business.services.api;


import com.konkerlabs.platform.registry.business.model.User;

public interface UserService {

    enum Validations {
        INVALID_USER_DETAILS("service.user.validation.detail.invalid"),
        INVALID_USER_EMAIL("service.user.validation.email.invalid"),
        INVALID_USER_NAME("service.user.validation.name.invalid"),
        INVALID_USER_PREFERENCE_DATEFORMAT("service.user.validation.dateformat.invalid"),
        INVALID_USER_PREFERENCE_LOCALE("service.user.validation.locale.invalid"),
        INVALID_USER_PREFERENCE_LANGUAGE("service.user.validation.language.invalid"),
        INVALID_PASSWORD_LENGTH("service.user.validation.password.invalid.lenght"),
        INVALID_PASSWORD_USER_DATA("service.user.validation.password.invalid.userdata"),
        INVALID_PASSWORD_CONFIRMATION("service.user.validation.password.invalid.confirmation"),
        INVALID_PASSWORD_INVALID("service.user.validation.password.invalid"),
        INVALID_PASSWORD_BLACKLISTED("service.user.validation.password.invalid.blacklisted"),
        NO_EXIST_USER("service.user.validation.no.exist");

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

    ServiceResponse<User> findByEmail(String email);
}
