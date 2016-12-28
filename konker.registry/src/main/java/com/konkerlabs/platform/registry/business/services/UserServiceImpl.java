package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.repositories.PasswordBlacklistRepository;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.UploadService;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.security.exceptions.SecurityException;
import com.konkerlabs.platform.security.managers.PasswordManager;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UploadService uploadService;
    @Autowired
    private PasswordBlacklistRepository passwordBlacklistRepository;

    private PasswordManager passwordManager;

    private Config passwordConfig = ConfigFactory.load().getConfig("password.user");

    public UserServiceImpl() {
        passwordManager = new PasswordManager();
    }

    @Override
    public ServiceResponse<User> save(User user,
                                      String oldPassword,
                                      String newPassword,
                                      String newPasswordConfirmation) {

        User fromStorage = userRepository.findOne(user.getEmail());

        if (!Optional.ofNullable(fromStorage).isPresent() ||
                !Optional.ofNullable(user.getEmail()).isPresent()
                || !user.getEmail().equals(fromStorage.getEmail())) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_EMAIL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(oldPassword).isPresent() ||
                !Optional.ofNullable(newPassword).isPresent() ||
                !Optional.ofNullable(newPasswordConfirmation).isPresent()) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_PASSWORD_CONFIRMATION.getCode())
                    .build();
        }

        if (!newPassword.equals(newPasswordConfirmation)) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_PASSWORD_CONFIRMATION.getCode())
                    .build();
        }

        if (!Optional.ofNullable(user).isPresent()) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_DETAILS.getCode())
                    .build();
        }

        if (!Optional.ofNullable(user.getName()).isPresent()) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_NAME.getCode())
                    .build();
        }
        if (!Optional.ofNullable(user.getDateFormat()).isPresent()) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_PREFERENCE_DATEFORMAT.getCode())
                    .build();
        }
        if (!Optional.ofNullable(user.getZoneId()).isPresent()) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_PREFERENCE_LOCALE.getCode())
                    .build();
        }
        if (!Optional.ofNullable(user.getLanguage()).isPresent()) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_PREFERENCE_LANGUAGE.getCode())
                    .build();
        }

        if(!StringUtils.isEmpty(user.getAvatar())
                && user.getAvatar().contains("data:image")) {
            user.setAvatar(uploadService.uploadBase64Img(user.getAvatar(), true).getResult());
        }

        if (!StringUtils.isEmpty(newPassword)) {
            try {
                validatePassword(user, fromStorage, oldPassword, newPassword, newPasswordConfirmation);
            } catch (BusinessException e) {
                return ServiceResponseBuilder.<User>error()
                        .withMessage(e.getMessage())
                        .build();

            }
        }

        Optional.ofNullable(newPasswordConfirmation).ifPresent(password -> {
            if (!StringUtils.isEmpty(newPasswordConfirmation)) {
                try {
                    user.setPassword(encodePassword(password));
                } catch (Exception e) {
                    LOG.error("Error encoding password for user " + user.getEmail());
                }
            }
        });

        if (Optional.ofNullable(user.getPassword()).isPresent() && !user.getPassword().startsWith(PasswordManager.QUALIFIER)) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Errors.ERROR_SAVE_USER.getCode()).build();
        }
        try {
            fillFrom(user, fromStorage);
            userRepository.save(fromStorage);
            Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                    .ifPresent(authentication -> {
                        fillFrom(fromStorage, (User) authentication.getPrincipal());
                    });

            return ServiceResponseBuilder.<User>ok().withResult(fromStorage).build();
        } catch (Exception e) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Errors.ERROR_SAVE_USER.getCode()).build();
        }

    }


    /**
     * Fill new values from form
     *
     * @param form
     * @param storage
     */
    private void fillFrom(User form, User storage) {
        storage.setLanguage(form.getLanguage());
        storage.setZoneId(form.getZoneId());
        storage.setAvatar(form.getAvatar());
        storage.setDateFormat(form.getDateFormat());
        storage.setPassword(!StringUtils.isEmpty(form.getPassword()) ? form.getPassword() : storage.getPassword());
        storage.setName(form.getName());
        storage.setPhone(form.getPhone());
    }

    /**
     * Encode the password
     *
     * @param password
     * @return String encoded password
     * @throws Exception
     */
    private String encodePassword(String password) throws Exception {
        if (!Optional.ofNullable(passwordManager).isPresent()) {
            passwordManager = new PasswordManager();
        }
        return passwordManager.createHash(
                password,
                Optional.of(passwordConfig.getInt("iterations"))
        );
    }

    /**
     * Validate password change rules
     *
     * @param fromForm
     * @param fromStorage
     * @throws BusinessException
     */
    private void validatePassword(User fromForm, User fromStorage, String oldPassword,
                                  String newPassword,
                                  String newPasswordConfirmation
    ) throws BusinessException {

        validateOldPassword(oldPassword, fromStorage);
        validatePasswordConfirmation(newPassword, newPasswordConfirmation);
        validatePasswordLenght(newPasswordConfirmation);
        validatePasswordPattern(fromForm.getUsername(), newPasswordConfirmation);
        validatePasswordBlackList(newPasswordConfirmation);

    }

    /**
     * Validate informed oldPassword compability with stored password
     *
     * @param oldPassword
     * @throws BusinessException
     */
    private void validateOldPassword(String oldPassword, User fromStorage) throws BusinessException {

        if (!Optional.ofNullable(passwordManager).isPresent()) {
            passwordManager = new PasswordManager();
        }

        if (!Optional.ofNullable(fromStorage).isPresent()) {
            throw new BusinessException(Validations.INVALID_USER_DETAILS.getCode());
        }

        try {
            if (!passwordManager.validatePassword(oldPassword, fromStorage.getPassword())) {
                throw new BusinessException(Validations.INVALID_PASSWORD_INVALID.getCode());

            }
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new BusinessException(Validations.INVALID_PASSWORD_USER_DATA.getCode());
        }

    }

    private void validatePasswordConfirmation(String newPassword, String newPasswordConfirmation)
            throws BusinessException {
        if (!newPassword.equals(newPasswordConfirmation)) {
            throw new BusinessException(Validations.INVALID_PASSWORD_CONFIRMATION.getCode());
        }
    }

    private void validatePasswordLenght(String password) throws BusinessException {
        if (password.length() < 12) {
            throw new BusinessException(Validations.INVALID_PASSWORD_LENGTH.getCode());
        }
    }

    private void validatePasswordPattern(String username, String password) throws BusinessException {
        if (password.equalsIgnoreCase(username)) {
            throw new BusinessException(Validations.INVALID_PASSWORD_USER_DATA.getCode());
        }
    }

    private void validatePasswordBlackList(String password) throws BusinessException {
        User.PasswordBlacklist matches =
                passwordBlacklistRepository.findOne(password);
        if (Optional.ofNullable(matches).isPresent()) {
            throw new BusinessException(Validations.INVALID_PASSWORD_BLACKLISTED.getCode());
        }
    }
}
