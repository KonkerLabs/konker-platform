package com.konkerlabs.platform.registry.business.services.api;

import java.util.List;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.UserNotification;

public interface UserNotificationService {
    enum Validations {
        USER_NOTIFICATION_NOT_FOUND("service.usernotifications.not_found"),
        USER_NULL("service.usernotifications.user_null"),
        USER_NOTIFICATION_NULL_UUID("service.usernotifications.null_uuid"),
        USER_NOTIFICATION_NULL("service.usernotifications.null"),
        USER_NOTIFICATION_POST_LAST_READ("service.usernotifications.post.last_read_not_null"),
        USER_NOTIFICATION_POST_READ("service.usernotifications.post.read")
        ;

        private String code;

        public String getCode() {
            return code;
        }

        Validations(String code) {
            this.code = code;
        }
    }
    
    ServiceResponse<Boolean> hasNewNotifications(User user);

    ServiceResponse<Boolean> unmarkHasNewNotifications(User user);

    ServiceResponse<List<UserNotification>> findAll(User user);

    ServiceResponse<List<UserNotification>> findUnread(User user);
    
    ServiceResponse<UserNotification> getUserNotification(User user, String notificationUuid);

    ServiceResponse<Boolean> markAllRead(User user);

    ServiceResponse<UserNotification> markRead(User user, String notificationUuid);

    ServiceResponse<UserNotification> markUnread(User user, String notificationUuid);

    ServiceResponse<UserNotification> postNotification(User user, UserNotification notification);
}
