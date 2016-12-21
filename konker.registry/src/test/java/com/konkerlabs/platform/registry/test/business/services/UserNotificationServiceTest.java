package com.konkerlabs.platform.registry.test.business.services;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventSchema;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.UserNotification;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.business.services.api.EventSchemaService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.UserNotificationService;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.base.RedisTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = { MongoTestConfiguration.class, BusinessTestConfiguration.class, RedisTestConfiguration.class })
@UsingDataSet(locations = { "/fixtures/users.json", "/fixtures/userNotifications.json" })
public class UserNotificationServiceTest extends BusinessLayerTestSupport {

    private static final class DescendingDateComparator implements Comparator<UserNotification> {
        // descending
        public int compare(UserNotification a, UserNotification b) {
            return b.getDate().compareTo(a.getDate());
        }
    }


    private static final String THE_USER_ID = "admin@konkerlabs.com";
    private static final String ANOTHER_USER_ID = "foo@konkerlabs.com";
    private static final String INEXISTENT_USER_ID = "e2c4e6d4-c774-11e6-bfc7-7f5b73e9b979@e91482c4-c774-11e6-ab7e-0ba7580e468e.com";

    private static final String SCHEDULED_MAINTENANCE_NOTIFICATION_UUID = "3619171e-c776-11e6-b1b3-0390d185f031";
    private static final String PAYMENT_PENDING_NOTIFICATION_UUID = "5bc88bb8-c783-11e6-ba41-67cba8a1ccf9";
    private static final String CHANGE_PASSWORD_NOTIFICATION_UUID = "ba5f1372-c783-11e6-a93f-733a6e9632e2";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserNotificationService userNotificationService;

    private User user;
    private User anotherUser;
    private User inexistentUser;

    private UserNotification scheduledMaintenaceNotification;
    private UserNotification paymentPendingNotification;
    private UserNotification changePasswordNotification;

    @Before
    public void setUp() throws Exception {
        user = userRepository.findOne(THE_USER_ID);
        anotherUser = userRepository.findOne(ANOTHER_USER_ID);
        inexistentUser = User.builder().email(INEXISTENT_USER_ID).build();

        scheduledMaintenaceNotification = UserNotification.builder().uuid(SCHEDULED_MAINTENANCE_NOTIFICATION_UUID)
                .destination(THE_USER_ID).date(Instant.ofEpochMilli(1482327799832L)).unread(Boolean.TRUE)
                .subject("Scheduled Maintenance").<UserNotification> build();
        paymentPendingNotification = UserNotification.builder().uuid(PAYMENT_PENDING_NOTIFICATION_UUID)
                .destination(THE_USER_ID).date(Instant.ofEpochMilli(1482328450375L)).unread(false).subject("Pagamento Pendente")
                .<UserNotification> build();
        changePasswordNotification = UserNotification.builder().uuid(CHANGE_PASSWORD_NOTIFICATION_UUID)
                .destination(THE_USER_ID).date(Instant.ofEpochMilli(1482328450375L)).unread(true).subject("Change your Password")
                .<UserNotification> build();
    }

    
    // ================================ getAll  ============================ //
    @Test
    public void shouldReturnNotificationsForTheUser() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.getAll(user);

        assertThat(notificationsResp, isResponseOk());

        List<UserNotification> notifications = notificationsResp.getResult();
        assertThat(notifications, hasItem(scheduledMaintenaceNotification));
        assertThat(notifications, hasItem(paymentPendingNotification));
        assertThat(notifications, hasItem(changePasswordNotification));
    }

    @Test
    public void shouldNotReturnNotificationsForOtherUsers() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.getAll(anotherUser);

        assertThat(notificationsResp, isResponseOk());
        
        List<UserNotification> notifications = notificationsResp.getResult();
        
        assertThat(notifications, not(empty()));
        
        assertThat(notifications, not(hasItem(scheduledMaintenaceNotification)));
    }

    @Test
    public void shouldNotReturnNotificationsForInexistentUsers() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.getAll(inexistentUser);

        assertThat(notificationsResp, isResponseOk());
        
        List<UserNotification> notifications = notificationsResp.getResult();

        assertThat(notifications, notNullValue());
        assertThat(notifications, empty());
    }

    
    @Test
    public void shouldReturnNotificationsForTheUserInDescendingOrder() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.getAll(user);
        List<UserNotification> notifications = notificationsResp.getResult();

        List<UserNotification> sortedCopy = new ArrayList<UserNotification>(notifications);
        
        Collections.sort(sortedCopy, new Comparator<UserNotification>() {
            // descending
            public int compare(UserNotification a, UserNotification b) {
                return b.getDate().compareTo(a.getDate());
            }
        });
        
        assertThat(notifications, equalTo(sortedCopy));
    }

    
    @Test
    public void shouldFailWhenUserIsNull() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.getAll(null);

        assertThat(notificationsResp, hasErrorMessage(UserNotificationService.Validations.USER_NULL.getCode()));
    }
    
    
    // ================================ getUnread  ============================ //
    @Test
    public void shouldReturnUnreadNotificationsForTheUser() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.getUnread(user);

        assertThat(notificationsResp, isResponseOk());

        List<UserNotification> notifications = notificationsResp.getResult();
        assertThat(notifications, hasItem(scheduledMaintenaceNotification));
        assertThat(notifications, hasItem(changePasswordNotification));
        assertThat(notifications, not(hasItem(paymentPendingNotification)));
    }

    @Test
    public void shouldNotReturnUnreadNotificationsForOtherUsers() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.getUnread(anotherUser);

        assertThat(notificationsResp, isResponseOk());
        
        List<UserNotification> notifications = notificationsResp.getResult();
        
        assertThat(notifications, not(empty()));
        assertThat(notifications, not(hasItem(scheduledMaintenaceNotification)));
    }

    @Test
    public void shouldNotReturnUnreadNotificationsForInexistentUsers() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.getUnread(inexistentUser);

        assertThat(notificationsResp, isResponseOk());
        
        List<UserNotification> notifications = notificationsResp.getResult();

        assertThat(notifications, notNullValue());
        assertThat(notifications, empty());
    }

    
    @Test
    public void shouldReturnUnreadNotificationsForTheUserInDescendingOrder() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.getUnread(user);
        List<UserNotification> notifications = notificationsResp.getResult();

        List<UserNotification> sortedCopy = new ArrayList<UserNotification>(notifications);
        
        Collections.sort(sortedCopy, new DescendingDateComparator());
        
        assertThat(notifications, equalTo(sortedCopy));
    }

    
    @Test
    public void shouldFailWhenUserForUnreadIsNull() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.getUnread(null);

        assertThat(notificationsResp, hasErrorMessage(UserNotificationService.Validations.USER_NULL.getCode()));
    }
    

}