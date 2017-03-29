package com.konkerlabs.platform.registry.web.converters.utils;

import java.text.MessageFormat;
import java.util.Optional;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.config.CdnConfig;

import lombok.Data;

@Component("userAvatarPath")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Data
public class UserAvatarPathUtil {
	
	private String absolutePath;

	@Autowired
	public UserAvatarPathUtil(User user, CdnConfig cdnConfig,  ServletContext servletContext) {
		if (cdnConfig.isEnabled() && Optional.ofNullable(user.getAvatar()).isPresent()) {
			absolutePath = cdnConfig.getPrefix() + "/" + cdnConfig.getName() + "/";
			absolutePath = absolutePath.concat(user.getAvatar());
		} else {
			absolutePath = MessageFormat.format("{0}/resources/konker/images/default-avatar.png", servletContext.getContextPath());
		}
	}

}
