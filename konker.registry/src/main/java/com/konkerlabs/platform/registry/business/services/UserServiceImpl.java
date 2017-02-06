package com.konkerlabs.platform.registry.business.services;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.amazonaws.util.Base64;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.repositories.PasswordBlacklistRepository;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.UploadService;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.config.PasswordUserConfig;
import com.konkerlabs.platform.security.managers.PasswordManager;

@Service
public class UserServiceImpl implements UserService {

    private Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UploadService uploadService;
    @Autowired
    private PasswordBlacklistRepository passwordBlacklistRepository;
    @Autowired
    private PasswordUserConfig passwordUserConfig;
    
    private PasswordManager passwordManager;


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

        if(!StringUtils.isEmpty(user.getAvatar())
                && user.getAvatar().contains("data:image")) {

        	String base64File = user.getAvatar();

    		String fileExt = base64File.split(",")[0].split("/")[1].split(";")[0];
    		String base64Content = base64File.split(",")[1];
    		
    		InputStream is = new ByteArrayInputStream(Base64.decode(base64Content.getBytes()));

    		ServiceResponse<InputStream> resizeReponse =  cropAndResizeAvatar(is, fileExt);
    		if (!resizeReponse.isOk()) {
                return ServiceResponseBuilder.<User>error()
                        .withMessages(resizeReponse.getResponseMessages())
                        .build();
    		}
    		
    		is = resizeReponse.getResult();
    		
            ServiceResponse<String> response = uploadService.upload(is, getUniqueFileName(), fileExt, true);
            if(!response.getStatus().equals(ServiceResponse.Status.OK)){
                return ServiceResponseBuilder.<User>error()
                        .withMessages(response.getResponseMessages())
                        .build();
            }
            user.setAvatar(response.getResult());
        } else {
            user.setAvatar(fromStorage.getAvatar());
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

	private String getUniqueFileName() {
		return UUID.randomUUID().toString();
	}

	private ServiceResponse<InputStream> cropAndResizeAvatar(InputStream is, String ext) {

		BufferedImage avatarUploaded = null;
		try {
			avatarUploaded = ImageIO.read(is);
		} catch (IOException e) {
			return ServiceResponseBuilder.<InputStream>error().withMessage(Validations.INVALID_AVATAR.getCode())
					.build();
		}

		// Crop
		int width = avatarUploaded.getWidth();
		int height = avatarUploaded.getHeight();

		BufferedImage centeredImage = null;

		if (height == width) {
			centeredImage = avatarUploaded;
		} else if (height > width) {
			int margin = (height - width) / 2;
			centeredImage = avatarUploaded.getSubimage(0, margin, width, width);
		} else {
			int margin = (width - height) / 2;
			centeredImage = avatarUploaded.getSubimage(margin, 0, height, height);
		}

		// Resize
		int newSize = (int) (centeredImage.getHeight() * 0.99 - 1);

		BufferedImage resizedImage = new BufferedImage(newSize, newSize, centeredImage.getType());
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(centeredImage, 0, 0, newSize, newSize, null);
		g.dispose();
		g.setComposite(AlphaComposite.Src);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// BufferedImage to InputStream
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(resizedImage, ext, os);
		} catch (IOException e) {
			return ServiceResponseBuilder.<InputStream>error()
					.withMessage(Validations.INVALID_AVATAR.getCode()).build();
		}
		InputStream resizedIs = new ByteArrayInputStream(os.toByteArray());
		
		return ServiceResponseBuilder.<InputStream>ok().withResult(resizedIs).build();

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
