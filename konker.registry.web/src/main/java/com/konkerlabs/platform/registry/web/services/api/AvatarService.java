package com.konkerlabs.platform.registry.web.services.api;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

public interface AvatarService {

	ServiceResponse<User> updateAvatar(User user);

}
