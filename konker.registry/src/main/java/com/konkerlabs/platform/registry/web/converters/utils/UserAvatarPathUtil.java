package com.konkerlabs.platform.registry.web.converters.utils;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.konkerlabs.platform.registry.business.model.User;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Data;

@Component("userAvatarPath")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Data
public class UserAvatarPathUtil {
	
	private static Config config = ConfigFactory.load().getConfig("cdn");

	private String absolutePath;
	
	@Autowired
	public UserAvatarPathUtil(User user) {
		if (Optional.ofNullable(user.getAvatar()).isPresent()) {
			absolutePath = config.getString("prefix") + "/" + config.getString("name") + "/";
			absolutePath = absolutePath.concat(user.getAvatar());
		} else {
			absolutePath = config.getString("defaultavatar");
		}
	}
}
