package com.konkerlabs.platform.registry.web.services;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.amazonaws.util.Base64;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.UserService.Validations;
import com.konkerlabs.platform.registry.web.services.api.AvatarService;
import com.konkerlabs.platform.registry.web.services.api.UploadService;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AvatarServiceImpl implements AvatarService {
	
	@Autowired
	private UploadService uploadService;
	
	@Autowired
	private UserRepository userRepository;

	@Override
    public ServiceResponse<User> updateAvatar(User user) {

    	User fromStorage = userRepository.findOne(user.getEmail());

        if(!StringUtils.isEmpty(user.getAvatar())
                && user.getAvatar().contains("data:image")) {

        	String base64File = user.getAvatar();

    		String fileExt = base64File.split(",")[0].split("/")[1].split(";")[0];
    		String base64Content = base64File.split(",")[1];
    		
    		InputStream is = new ByteArrayInputStream(Base64.decode(base64Content.getBytes()));

    		ServiceResponse<InputStream> resizeResponse =  cropAndResizeAvatar(is, fileExt);
    		if (!resizeResponse.isOk()) {
                return ServiceResponseBuilder.<User>error()
                        .withMessages(resizeResponse.getResponseMessages())
                        .build();
    		}
    		
    		is = resizeResponse.getResult();
    		
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

        return ServiceResponseBuilder.<User>ok()
                .withResult(user)
                .build();

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

}
