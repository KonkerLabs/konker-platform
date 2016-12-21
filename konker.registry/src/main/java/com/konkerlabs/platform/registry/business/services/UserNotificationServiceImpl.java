package com.konkerlabs.platform.registry.business.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.UserNotification;
import com.konkerlabs.platform.registry.business.repositories.UserNotificationRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.UserNotificationService;

@Service
public class UserNotificationServiceImpl implements UserNotificationService {
    @Autowired
    private UserNotificationRepository userNotificationRepository;

    @Override
    public ServiceResponse<Boolean> hasNewNotifications(User user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServiceResponse<List<UserNotification>> getAll(User user) {
        if (user == null) {
            return ServiceResponseBuilder.<List<UserNotification>> error().withMessage(Validations.USER_NULL.getCode())
                    .build();
        } else {
            List<UserNotification> notifications = userNotificationRepository.findByDestination(user.getEmail(),
                    new Sort(Sort.Direction.DESC, "date"));
            return ServiceResponseBuilder.<List<UserNotification>> ok().withResult(notifications).build();
        }
    }

    @Override
    public ServiceResponse<List<UserNotification>> getUnread(User user) {
        if (user == null) {
            return ServiceResponseBuilder.<List<UserNotification>> error().withMessage(Validations.USER_NULL.getCode())
                    .build();
        } else {
            List<UserNotification> notifications = userNotificationRepository.findUnreadByDestination(user.getEmail(),
                    new Sort(Sort.Direction.DESC, "date"));
            return ServiceResponseBuilder.<List<UserNotification>> ok().withResult(notifications).build();
        }
    }

    @Override
    public ServiceResponse<List<UserNotification>> markAllRead(User user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServiceResponse<UserNotification> markRead(User user, String notificationUuid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServiceResponse<UserNotification> markUnRead(User user, String notificationUuid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServiceResponse<UserNotification> postNotification(User user, UserNotification notification) {
        // TODO Auto-generated method stub
        return null;
    }

}
