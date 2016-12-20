package com.konkerlabs.platform.registry.business.services.api;

import java.util.List;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.UserNotification;

public interface UserNotificationService {
    enum Validations {
        TRANSFORMATION_NAME_IN_USE("service.usernotifications.not_found");
        
        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }
    
    ServiceResponse<List<UserNotification>> getAll(User user);

    ServiceResponse<List<UserNotification>> getUnread(User user);

    ServiceResponse<UserNotification> markRead(User user, String notificationUuid);

    ServiceResponse<UserNotification> markUnRead(User user, String notificationUuid);

    ServiceResponse<UserNotification> postNotification(User user, UserNotification notification);

    ServiceResponse<Boolean> hasNewNotifications(User user);
}
