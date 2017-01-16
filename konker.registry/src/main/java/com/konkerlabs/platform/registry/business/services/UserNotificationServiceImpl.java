package com.konkerlabs.platform.registry.business.services;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.UserNotification;
import com.konkerlabs.platform.registry.business.model.UserNotificationStatus;
import com.konkerlabs.platform.registry.business.repositories.UserNotificationRepository;
import com.konkerlabs.platform.registry.business.repositories.UserNotificationStatusRepository;
import com.konkerlabs.platform.registry.business.services.api.EmailService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.UserNotificationService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UserNotificationServiceImpl implements UserNotificationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserNotificationServiceImpl.class);
	
    private static final Sort SORT_DATE_DESC = new Sort(Sort.Direction.DESC, "date");
    @Autowired
    private UserNotificationRepository userNotificationRepository;

    @Autowired
    private UserNotificationStatusRepository userNotificationStatusRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private Environment environment;

    @Override
    public ServiceResponse<Boolean> hasNewNotifications(User user) {
        UserNotificationStatus status = userNotificationStatusRepository.getByDestination(user.getEmail());
        if (status == null || !status.getHasNewMessages()) {
            return ServiceResponseBuilder.<Boolean> ok().withResult(Boolean.FALSE).build();
        } else {
            return ServiceResponseBuilder.<Boolean> ok().withResult(Boolean.TRUE).build();
        }
    }

    @Override
    public ServiceResponse<List<UserNotification>> findAll(User user) {
        if (user == null) {
            return ServiceResponseBuilder.<List<UserNotification>> error().withMessage(Validations.USER_NULL.getCode())
                    .build();
        } else {
            List<UserNotification> notifications = userNotificationRepository.findByDestination(user.getEmail(),
                    SORT_DATE_DESC);
            return ServiceResponseBuilder.<List<UserNotification>> ok().withResult(notifications).build();
        }
    }

    @Override
    public ServiceResponse<List<UserNotification>> findUnread(User user) {
        if (user == null) {
            return ServiceResponseBuilder.<List<UserNotification>> error().withMessage(Validations.USER_NULL.getCode())
                    .build();
        } else {
            List<UserNotification> notifications = userNotificationRepository.findUnreadByDestination(user.getEmail(),
                    SORT_DATE_DESC);
            return ServiceResponseBuilder.<List<UserNotification>> ok().withResult(notifications).build();
        }
    }

    @Override
    public ServiceResponse<UserNotification> getUserNotification(User user, String notificationUuid) {
        if (user == null) {
            return ServiceResponseBuilder.<UserNotification> error().withMessage(Validations.USER_NULL.getCode())
                    .build();
        }
        if (notificationUuid == null) {
            return ServiceResponseBuilder.<UserNotification> error()
                    .withMessage(Validations.USER_NOTIFICATION_NULL_UUID.getCode()).build();
        } else {
            UserNotification notification = userNotificationRepository.getByDestinationAndUuid(user.getEmail(),
                    notificationUuid);
            if (notification != null) {
                return ServiceResponseBuilder.<UserNotification> ok().withResult(notification).build();
            } else {
                return ServiceResponseBuilder.<UserNotification> error()
                        .withMessage(Validations.USER_NOTIFICATION_NOT_FOUND.getCode()).build();
            }
        }
    }

    @Override
    public ServiceResponse<Boolean> markAllRead(User user) {
        if (user == null) {
            return ServiceResponseBuilder.<Boolean> error().withMessage(Validations.USER_NULL.getCode()).build();
        }

        // TODO: improve efficiency. We should not iterate on the application -
        // we should do that on the database level
        List<UserNotification> notifications = userNotificationRepository.findUnreadByDestination(user.getEmail(),
                SORT_DATE_DESC);
        notifications.forEach(UserNotification::markRead);
        userNotificationRepository.save(notifications);

        return ServiceResponseBuilder.<Boolean> ok().withResult(Boolean.TRUE).build();
    }

    @Override
    public ServiceResponse<UserNotification> markRead(User user, String notificationUuid) {
        return getAndApply(user, notificationUuid, (x) -> x.getUnread(), UserNotification::markRead);
    }

    private ServiceResponse<UserNotification> getAndApply(User user, String notificationUuid,
            Predicate<UserNotification> condition, Consumer<UserNotification> operation) {
        if (user == null) {
            return ServiceResponseBuilder.<UserNotification> error().withMessage(Validations.USER_NULL.getCode())
                    .build();
        }
        if (notificationUuid == null) {
            return ServiceResponseBuilder.<UserNotification> error()
                    .withMessage(Validations.USER_NOTIFICATION_NULL_UUID.getCode()).build();
        } else {
            UserNotification notification = userNotificationRepository.getByDestinationAndUuid(user.getEmail(),
                    notificationUuid);
            if (notification != null) {
                if (condition.test(notification)) {
                    operation.accept(notification);
                    notification = userNotificationRepository.save(notification);
                }
                return ServiceResponseBuilder.<UserNotification> ok().withResult(notification).build();
            } else {
                return ServiceResponseBuilder.<UserNotification> error()
                        .withMessage(Validations.USER_NOTIFICATION_NOT_FOUND.getCode()).build();
            }
        }

    }

    @Override
    public ServiceResponse<UserNotification> markUnread(User user, String notificationUuid) {
        return getAndApply(user, notificationUuid, x -> !x.getUnread(), UserNotification::markUnread);
    }

    @Override
    public ServiceResponse<UserNotification> postNotification(User user, UserNotification notification) {
        if (user == null) {
            return ServiceResponseBuilder.<UserNotification> error().withMessage(Validations.USER_NULL.getCode())
                    .build();
        } else if (notification == null) {
            return ServiceResponseBuilder.<UserNotification> error()
                    .withMessage(Validations.USER_NOTIFICATION_NULL_UUID.getCode()).build();
        } else if (notification.getLastReadDate() != null) {
            return ServiceResponseBuilder.<UserNotification> error()
                    .withMessage(Validations.USER_NOTIFICATION_POST_LAST_READ.getCode()).build();
        } else {
            String correlationUuid = notification.getCorrelationUuid() != null ? notification.getCorrelationUuid()
                    : UUID.randomUUID().toString();
            Instant date = notification.getDate() != null ? notification.getDate() : Instant.now();

            UserNotification fresh = UserNotification.buildFresh(notification.getDestination(),
                    notification.getSubject(), notification.getContentLanguage(), notification.getContentType(), date,
                    correlationUuid, notification.getBody());

            UserNotification saved = userNotificationRepository.save(fresh);

            UserNotificationStatus userHasNewMessagesFlag = userNotificationStatusRepository
                    .getByDestination(user.getEmail());
            
            if (userHasNewMessagesFlag == null) {
                userHasNewMessagesFlag = UserNotificationStatus.builder().destination(user.getEmail()).build();
            }
            userHasNewMessagesFlag.markHasNewMessages(saved.getUuid());
            userNotificationStatusRepository.save(userHasNewMessagesFlag);

            sendEmailNotification(user, saved);

            return ServiceResponseBuilder.<UserNotification> ok().withResult(saved).build();
        }

    }

	private void sendEmailNotification(User user, UserNotification saved) {
		Config config = ConfigFactory.load().getConfig("email");
		List<String> profiles = Arrays.stream(environment.getActiveProfiles()).collect(Collectors.toList());
		
		if (user.isNotificationViaEmail() && profiles.contains("email")) {
			Map<String, Object> templateParam = new HashMap<>();
			templateParam.put("name", user.getName());
			templateParam.put("body", saved.getBody());
			
			try {
				emailService.send(config.getString("sender"), 
						Collections.singletonList(user), 
						Collections.emptyList(), 
						saved.getSubject(), 
						"text/email-notification", 
						templateParam , 
						user.getLanguage().getLocale());
			} catch (MessagingException e) {
				LOGGER.error("Notification: ", user.getTenant().toURI(), user.getTenant().getLogLevel(), e);
			}
		}
	}

    @Override
    public ServiceResponse<Boolean> unmarkHasNewNotifications(User user) {
        UserNotificationStatus userHasNewMessagesFlag = userNotificationStatusRepository
                .getByDestination(user.getEmail());
        if (userHasNewMessagesFlag == null) {
            userHasNewMessagesFlag = UserNotificationStatus.builder().destination(user.getEmail()).build();
        }
        userHasNewMessagesFlag.unmarkHasNewMessages();
        userNotificationStatusRepository.save(userHasNewMessagesFlag);

        return ServiceResponseBuilder.<Boolean> ok().withResult(Boolean.FALSE).build();
    }

}
