package com.konkerlabs.platform.registry.business.services;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.MessageFormat;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.repositories.PasswordBlacklistRepository;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.config.PasswordUserConfig;
import com.konkerlabs.platform.security.managers.PasswordManager;

@Service
public class UserServiceImpl implements UserService {

    private Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordBlacklistRepository passwordBlacklistRepository;

    private PasswordUserConfig passwordUserConfig; 

    private PasswordManager passwordManager;

    public UserServiceImpl() {
        passwordUserConfig = new PasswordUserConfig(); 
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

            LOG.debug("This user id is ivalid:" + (Optional.ofNullable(user.getEmail()).isPresent() ? user.getEmail() : "NULL"));
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_EMAIL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(oldPassword).isPresent() ||
                !Optional.ofNullable(newPassword).isPresent() ||
                !Optional.ofNullable(newPasswordConfirmation).isPresent()) {

            LOG.debug("Invalid password confirmation",
                    fromStorage.getTenant().toURI(),
                    fromStorage.getTenant().getLogLevel(),
                    fromStorage);
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_PASSWORD_CONFIRMATION.getCode())
                    .build();
        }

        if (!StringUtils.isEmpty(newPassword)) {
            try {
                validateOldPassword(oldPassword, fromStorage);
            } catch (BusinessException e) {
                LOG.debug("Invalid current password",
                        fromStorage.getTenant().toURI(),
                        fromStorage.getTenant().getLogLevel());
                return ServiceResponseBuilder.<User>error()
                        .withMessage(e.getMessage())
                        .build();

            }
        }
        
        return save(user, newPassword, newPasswordConfirmation);

    }


	@Override
	public ServiceResponse<User> save(User user, String newPassword, String newPasswordConfirmation) {
    	User fromStorage = userRepository.findOne(user.getEmail());

        if (!Optional.ofNullable(fromStorage).isPresent() ||
                !Optional.ofNullable(user.getEmail()).isPresent()
                || !user.getEmail().equals(fromStorage.getEmail())) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_EMAIL.getCode())
                    .build();
        }

        if (!newPassword.equals(newPasswordConfirmation)) {
            LOG.debug("Invalid password confirmation on user update",
                    fromStorage.getTenant().toURI(),
                    fromStorage.getTenant().getLogLevel(),
                    fromStorage);

            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_PASSWORD_CONFIRMATION.getCode())
                    .build();
        }

        if (!Optional.ofNullable(user).isPresent()) {
            LOG.debug("Invalid user details on update",
                    fromStorage.getTenant().toURI(),
                    fromStorage.getTenant().getLogLevel(), fromStorage);

            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_DETAILS.getCode())
                    .build();
        }

        if (!Optional.ofNullable(user.getDateFormat()).isPresent()) {
            LOG.debug("Invalid date format preference update",
                    fromStorage.getTenant().toURI(),
                    fromStorage.getTenant().getLogLevel(),
                    fromStorage);

            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_PREFERENCE_DATEFORMAT.getCode())
                    .build();
        }
        if (!Optional.ofNullable(user.getZoneId()).isPresent()) {
            LOG.debug("Invalid locale preference update",
                    fromStorage.getTenant().toURI(),
                    fromStorage.getTenant().getLogLevel(),
                    fromStorage);

            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_PREFERENCE_LOCALE.getCode())
                    .build();
        }
        if (!Optional.ofNullable(user.getLanguage()).isPresent()) {
            LOG.debug("Invalid language preference update",
                    fromStorage.getTenant().toURI(),
                    fromStorage.getTenant().getLogLevel(),
                    fromStorage);

            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_PREFERENCE_LANGUAGE.getCode())
                    .build();
        }

        if (!StringUtils.isEmpty(newPassword)) {
            try {
                validatePassword(user, fromStorage, newPassword, newPasswordConfirmation);
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
                    LOG.info(MessageFormat.format("User password has been changed, user \"{0}\"",
                			fromStorage.getEmail()), 
                			fromStorage.getTenant().toURI(), 
                			fromStorage.getTenant().getLogLevel(), 
                			fromStorage);
                } catch (Exception e) {
                    LOG.error("Error encoding password for user " + fromStorage.getEmail(), 
                    		fromStorage.getTenant().toURI(), fromStorage.getTenant().getLogLevel(), fromStorage);
                }
            }
        });

        if (Optional.ofNullable(user.getPassword()).isPresent() && !user.getPassword().startsWith(PasswordManager.QUALIFIER)) {
            LOG.debug(Errors.ERROR_SAVE_USER.getCode(), fromStorage.getTenant().toURI(), fromStorage.getTenant().getLogLevel(), fromStorage);

            return ServiceResponseBuilder.<User>error()
                    .withMessage(Errors.ERROR_SAVE_USER.getCode()).build();
        }
        try {
            fillFrom(user, fromStorage);
            userRepository.save(fromStorage);
            Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                    .ifPresent(authentication -> {
                        User principal = (User) Optional.ofNullable(authentication.getPrincipal())
                        		.filter(p -> !p.equals("anonymousUser")).orElse(User.builder().build());

						fillFrom(fromStorage, principal);
                    });

            return ServiceResponseBuilder.<User>ok().withResult(fromStorage).build();
        } catch (Exception e) {
            LOG.debug("Error saving User update",
                    fromStorage.getTenant().toURI(),
                    fromStorage.getTenant().getLogLevel(), fromStorage);
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
        storage.setNotificationViaEmail(form.isNotificationViaEmail());
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
                Optional.of(passwordUserConfig.getIterations())
        );
    }

    /**
     * Validate password change rules
     *
     * @param fromForm
     * @param fromStorage
     * @throws BusinessException
     */
    private void validatePassword(User fromForm, User fromStorage,
                                  String newPassword,
                                  String newPasswordConfirmation
    ) throws BusinessException {

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

	@Override
	public ServiceResponse<User> findByEmail(String email) {
		if (!Optional.ofNullable(email).isPresent()) {
			return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.NO_EXIST_USER.getCode()).build();
		}
		
		User user = userRepository.findOne(email);
		return ServiceResponseBuilder.<User>ok().withResult(user).build();
	}

}
