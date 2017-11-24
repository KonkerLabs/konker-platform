package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Role;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import com.konkerlabs.platform.registry.business.repositories.PasswordBlacklistRepository;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.PasswordUserConfig;
import com.konkerlabs.platform.security.managers.PasswordManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private static final String EMAIL_PATTERN =
    		"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
    		+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    
    private Pattern patternEmail;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordBlacklistRepository passwordBlacklistRepository;
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private EmailConfig emailConfig;
    
    @Autowired
    private MessageSource messageSource;

    private PasswordUserConfig passwordUserConfig; 

    private PasswordManager passwordManager;

    public UserServiceImpl() {
        passwordUserConfig = new PasswordUserConfig(); 
        passwordManager = new PasswordManager();
        patternEmail = Pattern.compile(EMAIL_PATTERN);
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

            LOG.debug("This user id is invalid:" + (Optional.ofNullable(user.getEmail()).isPresent() ? user.getEmail() : "NULL"));
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

        ServiceResponse<User> errorResponse = checkNullFields(user);
        if (errorResponse != null) return errorResponse;

        if (!Optional.ofNullable(newPassword).isPresent()) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_PASSWORD_INVALID.getCode())
                    .build();
        }

        if (!newPassword.equals(newPasswordConfirmation)) {
            LOG.debug("Invalid password confirmation on user update",
                    user.getTenant().toURI(),
                    user.getTenant().getLogLevel(),
                    user);

            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_PASSWORD_CONFIRMATION.getCode())
                    .build();
        }

        User fromStorage = Optional.ofNullable(userRepository.findOne(user.getEmail())).orElse(user);

        if (!Optional.ofNullable(user.getEmail()).isPresent() ||
                !user.getEmail().equals(fromStorage.getEmail())) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_EMAIL.getCode())
                    .build();
        }

        if (!StringUtils.isEmpty(newPassword)) {
            try {
                validatePassword(user, newPassword, newPasswordConfirmation);
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

        if (Optional.ofNullable(user.getPassword()).isPresent() && !passwordManager.validateHash(user.getPassword())) {
            LOG.debug(Errors.ERROR_SAVE_USER.getCode(), fromStorage.getTenant().toURI(), fromStorage.getTenant().getLogLevel(), fromStorage);

            return ServiceResponseBuilder.<User>error()
                    .withMessage(Errors.ERROR_SAVE_USER.getCode()).build();
        }

        fillFrom(user, fromStorage);
        return persistValidUser(fromStorage);
    }

    private ServiceResponse<User> saveWithPasswordHash(User user, String passwordHash) {

        ServiceResponse<User> errorResponse = checkNullFields(user);
        if (errorResponse != null) return errorResponse;

        User fromStorage = Optional.ofNullable(userRepository.findOne(user.getEmail())).orElse(user);

        if (!Optional.ofNullable(user.getEmail()).isPresent() ||
                !user.getEmail().equals(fromStorage.getEmail())) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_EMAIL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(passwordHash).isPresent()) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_PASSWORD_HASH_INVALID.getCode())
                    .build();
        }

        if (!passwordManager.validateHash(passwordHash)) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_PASSWORD_HASH_INVALID.getCode())
                    .build();
        } else {
            user.setPassword(passwordHash);
        }

        fillFrom(user, fromStorage);
        return persistValidUser(fromStorage);
    }

    private ServiceResponse<User> persistValidUser(User user) {
        try {
            userRepository.save(user);

            if (SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken) {
                Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                        .ifPresent(authentication -> {
                            User principal = (User) Optional.ofNullable(authentication.getPrincipal())
                                    .filter(p -> !p.equals("anonymousUser")).orElse(User.builder().build());

                            fillFrom(user, principal);
                        });
            }

            return ServiceResponseBuilder.<User>ok().withResult(user).build();
        } catch (Exception e) {
            LOG.debug("Error saving User update",
                    user.getTenant().toURI(),
                    user.getTenant().getLogLevel(), user);
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Errors.ERROR_SAVE_USER.getCode()).build();
        }
    }

    private ServiceResponse<User> checkNullFields(User user) {

        if (!Optional.ofNullable(user).isPresent()) {
            LOG.debug("Invalid user details on update",
                    user.getTenant().toURI(),
                    user.getTenant().getLogLevel(),
                    user);

            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_DETAILS.getCode())
                    .build();
        }

        if (!Optional.ofNullable(user.getEmail()).isPresent() ||
                !patternEmail.matcher(user.getEmail()).matches()) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_EMAIL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(user.getDateFormat()).isPresent()) {
            LOG.debug("Invalid date format preference update",
                    user.getTenant().toURI(),
                    user.getTenant().getLogLevel(),
                    user);

            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_PREFERENCE_DATEFORMAT.getCode())
                    .build();
        }

        if (!Optional.ofNullable(user.getZoneId()).isPresent()) {
            LOG.debug("Invalid locale preference update",
                    user.getTenant().toURI(),
                    user.getTenant().getLogLevel(),
                    user);

            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_PREFERENCE_LOCALE.getCode())
                    .build();
        }

        if (!Optional.ofNullable(user.getLanguage()).isPresent()) {
            LOG.debug("Invalid language preference update",
                    user.getTenant().toURI(),
                    user.getTenant().getLogLevel(),
                    user);

            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_PREFERENCE_LANGUAGE.getCode())
                    .build();
        }

        return null;
    }

    @Override
    public ServiceResponse<User> createAccount(User user, String newPassword, String newPasswordConfirmation) {

        if (validateUserCreationLimit()) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_LIMIT_CREATION.getCode())
                    .build();
        }

        // must validate password here also to don't leak the user existence
        try {
            validatePassword(user, newPassword, newPasswordConfirmation);
        } catch (BusinessException e) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(e.getMessage())
                    .build();
        }

        User fromStorage = userRepository.findByEmail(user.getEmail());
		
		if (Optional.ofNullable(fromStorage).isPresent() && fromStorage.isActive()) {
            return sendAccountExistsEmail(fromStorage);
		} else if (Optional.ofNullable(fromStorage).isPresent()) {
			sendValidateTokenEmail(fromStorage);
		}
		
		if (user.getName() == null || user.getName().isEmpty()) {
			return ServiceResponseBuilder.<User>error()
					.withMessage(Validations.INVALID_USER_NAME.getCode())
					.build();
		}
		
		if (!Optional.ofNullable(user.getTenant().getName()).isPresent()){
			user.getTenant().setName(user.getName());
		}
		
		ServiceResponse<Tenant> serviceResponse = tenantService.save(user.getTenant());
		user.setTenant(serviceResponse.getResult());
		
		ServiceResponse<Role> roleResponse = roleService.findByName(RoleService.ROLE_IOT_USER);
		user.setRoles(Collections.singletonList(roleResponse.getResult()));
		user.setZoneId(TimeZone.AMERICA_SAO_PAULO);
		user.setDateFormat(DateFormat.YYYYMMDD);
		user.setRegistrationDate(Instant.now());
		
    	ServiceResponse<User> save = save(user, newPassword, newPasswordConfirmation);
    	
    	if (save.isOk()) {
            sendValidateTokenEmail(user);
    	}
    	
		return save;
    }

    private ServiceResponse<User> sendAccountExistsEmail(User fromStorage) {
        Map<String, Object> templateParam = new HashMap<>();
        templateParam.put("link", emailConfig.getBaseurl().concat("login"));
        templateParam.put("name", fromStorage.getName());

        sendMail(fromStorage, templateParam, Messages.USER_HAS_ACCOUNT,"html/email-accountalreadyexists");
        return ServiceResponseBuilder.<User>ok()
                .withResult(fromStorage)
                .build();
    }

    private void sendValidateTokenEmail(User user) {
        ServiceResponse<String> responseToken = tokenService.generateToken(
                TokenService.Purpose.VALIDATE_EMAIL,
                user,
                Duration.ofDays(2L));

        Map<String, Object> templateParam = new HashMap<>();
        templateParam.put("link", emailConfig.getBaseurl().concat("subscription/").concat(responseToken.getResult()));
        templateParam.put("name", user.getName());

        sendMail(user, templateParam, Messages.USER_SUBJECT_MAIL, "html/email-selfsubscription");
    }

    private boolean validateUserCreationLimit() {
        Instant start = LocalDateTime
                .now()
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .toInstant(ZoneOffset.UTC);
        Instant end = LocalDateTime
                .now()
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .toInstant(ZoneOffset.UTC);

        Long countUsers = userRepository.countRegistrationsBetweenDate(start, end);

        return countUsers >= 250L;
    }

    private void sendMail(User user, Map<String, Object> templateParam, Messages message, String templateName) {
		emailService.send(
				emailConfig.getSender(), 
				Collections.singletonList(user), 
				Collections.emptyList(), 
				messageSource.getMessage(message.getCode(), null, user.getLanguage().getLocale()), 
				templateName, 
				templateParam, 
				user.getLanguage().getLocale());
	}
    
    @Override
    public ServiceResponse<User> createAccountWithPasswordHash(User user, String passwordHash) {

        if (validateUserCreationLimit()) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_LIMIT_CREATION.getCode())
                    .build();
        }


        // must validate hash here also to don't leak the user existence
        if (!Optional.ofNullable(passwordHash).isPresent()) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_PASSWORD_HASH_INVALID.getCode())
                    .build();
        }

        if (!passwordManager.validateHash(passwordHash)) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_PASSWORD_HASH_INVALID.getCode())
                    .build();
        }

        User fromStorage = userRepository.findByEmail(user.getEmail());

        if (Optional.ofNullable(fromStorage).isPresent()) {
            return sendAccountExistsEmail(fromStorage);
        }

        if (user.getName() == null || user.getName().isEmpty()) {
            return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.INVALID_USER_NAME.getCode())
                    .build();
        }

        if (!Optional.ofNullable(user.getTenant().getName()).isPresent()){
            user.getTenant().setName(user.getName());
        }

        ServiceResponse<Tenant> serviceResponse = tenantService.save(user.getTenant());
        user.setTenant(serviceResponse.getResult());

        ServiceResponse<User> save = saveWithPasswordHash(user, passwordHash);

        if (save.isOk()) {
            sendValidateTokenEmail(user);
        }

        return save;

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
        
        if (!storage.isActive()) {
        	storage.setActive(form.isActive());
        }
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
     */
    private void validatePassword(User fromForm,
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
		if (!Optional.ofNullable(email).isPresent() || email.isEmpty()) {
			return ServiceResponseBuilder.<User>error()
                    .withMessage(Validations.NO_EXIST_USER.getCode()).build();
		}
		
		User user = userRepository.findOne(email);
		return ServiceResponseBuilder.<User>ok().withResult(user).build();
	}

	@Override
	public ServiceResponse<List<User>> findAll(Tenant tenant) {
		return ServiceResponseBuilder
				.<List<User>>ok()
				.withResult(userRepository.findAllByTenantId(tenant.getId()))
				.build();
	}

	@Override
	public ServiceResponse<User> findByTenantAndEmail(Tenant tenant, String email) {
		User user = userRepository.findAllByTenantIdAndEmail(tenant.getId(), email);
		
		if (!Optional.ofNullable(user).isPresent()) {
			return ServiceResponseBuilder
					.<User>error()
					.withMessage(Validations.NO_EXIST_USER.getCode())
					.build();
		}
		
		return ServiceResponseBuilder
				.<User>ok()
				.withResult(user)
				.build();
	}

}
