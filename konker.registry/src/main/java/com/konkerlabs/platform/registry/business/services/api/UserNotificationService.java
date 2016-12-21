package com.konkerlabs.platform.registry.business.services.api;

import java.util.List;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.UserNotification;

public interface UserNotificationService {
    enum Validations {
        USER_NOT_FOUND("service.usernotifications.not_found"),
        USER_NULL("service.usernotifications.user_null");

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }
    
    ServiceResponse<Boolean> hasNewNotifications(User user);

    ServiceResponse<List<UserNotification>> getAll(User user);

    ServiceResponse<List<UserNotification>> getUnread(User user);

    ServiceResponse<List<UserNotification>> markAllRead(User user);

    ServiceResponse<UserNotification> markRead(User user, String notificationUuid);

    ServiceResponse<UserNotification> markUnRead(User user, String notificationUuid);

    ServiceResponse<UserNotification> postNotification(User user, UserNotification notification);
}
