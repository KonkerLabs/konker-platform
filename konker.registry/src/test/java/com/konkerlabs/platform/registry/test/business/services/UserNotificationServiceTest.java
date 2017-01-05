package com.konkerlabs.platform.registry.test.business.services;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.UserNotification;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.UserNotificationService;
import com.konkerlabs.platform.registry.business.services.api.UserNotificationService.Validations;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.base.RedisTestConfiguration;
import com.konkerlabs.platform.registry.test.base.SpringMailTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = { MongoTestConfiguration.class, BusinessTestConfiguration.class, RedisTestConfiguration.class,
                SpringMailTestConfiguration.class })
@UsingDataSet(locations = { "/fixtures/users.json", "/fixtures/userNotifications.json" })
@ActiveProfiles("email")
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
    
    private static final String POSTED_CORRELATION_UUID = "5fa49178-c85f-11e6-9f92-4f0a7c448bbc";

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

    private static Predicate<UserNotification> isUnreadNotification = x -> x.getUnread() && x.getLastReadDate() == null;
    private static Predicate<UserNotification> isReadNotification = isUnreadNotification.negate();

    @Before
    public void setUp() throws Exception {
        user = userRepository.findOne(THE_USER_ID);
        anotherUser = userRepository.findOne(ANOTHER_USER_ID);
        inexistentUser = User.builder().email(INEXISTENT_USER_ID).build();

        scheduledMaintenaceNotification = UserNotification.builder().uuid(SCHEDULED_MAINTENANCE_NOTIFICATION_UUID)
                .destination(THE_USER_ID).date(Instant.ofEpochMilli(1482327799832L)).unread(Boolean.TRUE)
                .subject("Scheduled Maintenance").<UserNotification> build();
        paymentPendingNotification = UserNotification.builder().uuid(PAYMENT_PENDING_NOTIFICATION_UUID)
                .destination(THE_USER_ID).date(Instant.ofEpochMilli(1482328450375L)).unread(false)
                .subject("Pagamento Pendente").<UserNotification> build();
        changePasswordNotification = UserNotification.builder().uuid(CHANGE_PASSWORD_NOTIFICATION_UUID)
                .destination(THE_USER_ID).date(Instant.ofEpochMilli(1482328450375L)).unread(true)
                .subject("Change your Password").<UserNotification> build();
    }

    // ================================ getAll ============================ //
    @Test
    public void shouldReturnNotificationsForTheUser() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.findAll(user);

        assertThat(notificationsResp, isResponseOk());

        List<UserNotification> notifications = notificationsResp.getResult();
        assertThat(notifications, hasItem(scheduledMaintenaceNotification));
        assertThat(notifications, hasItem(paymentPendingNotification));
        assertThat(notifications, hasItem(changePasswordNotification));
    }

    @Test
    public void shouldNotReturnNotificationsForOtherUsers() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.findAll(anotherUser);

        assertThat(notificationsResp, isResponseOk());

        List<UserNotification> notifications = notificationsResp.getResult();

        assertThat(notifications, not(empty()));

        assertThat(notifications, not(hasItem(scheduledMaintenaceNotification)));
    }

    @Test
    public void shouldNotReturnNotificationsForInexistentUsers() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.findAll(inexistentUser);

        assertThat(notificationsResp, isResponseOk());

        List<UserNotification> notifications = notificationsResp.getResult();

        assertThat(notifications, notNullValue());
        assertThat(notifications, empty());
    }

    @Test
    public void shouldReturnNotificationsForTheUserInDescendingOrder() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.findAll(user);
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
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.findAll(null);

        assertThat(notificationsResp, hasErrorMessage(UserNotificationService.Validations.USER_NULL.getCode()));
    }

    // ============================= getUnread ========================= //
    @Test
    public void shouldReturnUnreadNotificationsForTheUser() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.findUnread(user);

        assertThat(notificationsResp, isResponseOk());

        List<UserNotification> notifications = notificationsResp.getResult();
        assertThat(notifications, hasItem(scheduledMaintenaceNotification));
        assertThat(notifications, hasItem(changePasswordNotification));
        assertThat(notifications, not(hasItem(paymentPendingNotification)));
    }

    @Test
    public void shouldNotReturnUnreadNotificationsForOtherUsers() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.findUnread(anotherUser);

        assertThat(notificationsResp, isResponseOk());

        List<UserNotification> notifications = notificationsResp.getResult();

        assertThat(notifications, not(empty()));
        assertThat(notifications, not(hasItem(scheduledMaintenaceNotification)));
    }

    @Test
    public void shouldNotReturnUnreadNotificationsForInexistentUsers() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.findUnread(inexistentUser);

        assertThat(notificationsResp, isResponseOk());

        List<UserNotification> notifications = notificationsResp.getResult();

        assertThat(notifications, notNullValue());
        assertThat(notifications, empty());
    }

    @Test
    public void shouldReturnUnreadNotificationsForTheUserInDescendingOrder() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.findUnread(user);
        List<UserNotification> notifications = notificationsResp.getResult();

        List<UserNotification> sortedCopy = new ArrayList<UserNotification>(notifications);

        Collections.sort(sortedCopy, new DescendingDateComparator());

        assertThat(notifications, equalTo(sortedCopy));
    }

    @Test
    public void shouldFailWhenUserForUnreadIsNull() throws Exception {
        ServiceResponse<List<UserNotification>> notificationsResp = userNotificationService.findUnread(null);

        assertThat(notificationsResp, hasErrorMessage(UserNotificationService.Validations.USER_NULL.getCode()));
    }

    // ======================= getUserNotification ========================= //
    @Test
    public void shouldReturnSingleNotifications() throws Exception {
        ServiceResponse<UserNotification> notificationsResp = userNotificationService.getUserNotification(user,
                PAYMENT_PENDING_NOTIFICATION_UUID);

        assertThat(notificationsResp, isResponseOk());
        UserNotification notification = notificationsResp.getResult();

        assertThat(notification, equalTo(paymentPendingNotification));
    }

    @Test
    public void shouldNotReturnSingleNotificationsForOtherUsers() throws Exception {
        ServiceResponse<UserNotification> notificationsResp = userNotificationService.getUserNotification(anotherUser,
                PAYMENT_PENDING_NOTIFICATION_UUID);

        assertThat(notificationsResp, hasErrorMessage(Validations.USER_NOTIFICATION_NOT_FOUND.getCode()));
    }

    @Test
    public void shouldFailReturnSingleForNullUser() throws Exception {
        ServiceResponse<UserNotification> notificationsResp = userNotificationService.getUserNotification(null,
                PAYMENT_PENDING_NOTIFICATION_UUID);

        assertThat(notificationsResp, hasErrorMessage(Validations.USER_NULL.getCode()));
    }

    @Test
    public void shouldFailReturnSingleForNullUuid() throws Exception {
        ServiceResponse<UserNotification> notificationsResp = userNotificationService.getUserNotification(user, null);

        assertThat(notificationsResp, hasErrorMessage(Validations.USER_NOTIFICATION_NULL_UUID.getCode()));
    }

    // ======================= markAllRead ========================= //
    @Test
    public void shouldMarkAllReadForAUserMarkAllRead() throws Exception {
        // Validates that at least two notifications are marked as unread as
        // precondition
        assertThat(userNotificationService.getUserNotification(user, CHANGE_PASSWORD_NOTIFICATION_UUID).getResult()
                .getUnread(), equalTo(Boolean.TRUE));
        assertThat(userNotificationService.getUserNotification(user, SCHEDULED_MAINTENANCE_NOTIFICATION_UUID)
                .getResult().getUnread(), equalTo(Boolean.TRUE));

        // the method should return a boolean - no error
        ServiceResponse<Boolean> markedResponse = userNotificationService.markAllRead(user);
        assertThat(markedResponse, isResponseOk());

        // Validates that all notifications are marked as read now
        List<UserNotification> notifications = userNotificationService.findAll(user).getResult();
        assertThat("All should be read", notifications.stream().allMatch(isReadNotification));

        // Validates that the two original notifications are still listed (they
        // didn't disappear)
        Set<String> uuids = notifications.stream().map(UserNotification::getUuid).collect(Collectors.toSet());
        assertThat(uuids, hasItem(CHANGE_PASSWORD_NOTIFICATION_UUID));
        assertThat(uuids, hasItem(SCHEDULED_MAINTENANCE_NOTIFICATION_UUID));
    }

    @Test
    public void shouldNotMarkReadForOtherUsers() throws Exception {
        // Validates that at least one notification is marked as unread for
        // other user
        List<UserNotification> notificationsPreChange = userNotificationService.findAll(anotherUser).getResult();
        assertThat("At least one should be unread, as a precondition for this test",
                notificationsPreChange.stream().anyMatch(isUnreadNotification));

        // marks all read for the user - this should not change other users
        ServiceResponse<Boolean> markedResponse = userNotificationService.markAllRead(user);
        assertThat(markedResponse, isResponseOk());

        // Validates that all notifications are marked as read now
        List<UserNotification> notificationsPostChange = userNotificationService.findAll(anotherUser).getResult();
        assertThat("The notifications for other users should not be marked read",
                notificationsPostChange.stream().anyMatch(isUnreadNotification));
    }

    @Test
    public void shouldFailMarkAllReadForNullUser() throws Exception {
        ServiceResponse<Boolean> notificationsResp = userNotificationService.markAllRead(null);
        assertThat(notificationsResp, hasErrorMessage(Validations.USER_NULL.getCode()));
    }

    @Test
    public void shouldBeIdempotentWhenCallingMarkRead() throws Exception {
        // marks all read for the user
        ServiceResponse<Boolean> markedResponse = userNotificationService.markAllRead(user);
        assertThat(markedResponse, isResponseOk());

        // Validates that all notifications are marked as read now
        List<UserNotification> notificationsPostChange = userNotificationService.findAll(user).getResult();
        assertThat("The notifications should all be read afiter this call",
                notificationsPostChange.stream().allMatch(isReadNotification));

        // if called a second time, they should still be marked with unread =
        // false.
        // It is not a toggle - it should be idempotent
        ServiceResponse<Boolean> markedResponseAgain = userNotificationService.markAllRead(user);
        assertThat(markedResponseAgain, isResponseOk());

        // Validates that all notifications are marked as read now
        List<UserNotification> notificationsPostChangeAgain = userNotificationService.findAll(user).getResult();
        assertThat("Calling mark read two times in sequence should be idempotent",
                notificationsPostChangeAgain.stream().allMatch(isReadNotification));
    }

    @Test
    public void shouldUpdateLastReadTimeWhenMarkingAllRead() throws Exception {
        // validate as a precondition that some messages have undefined last
        // read time
        List<UserNotification> notificationsPreChange = userNotificationService.findAll(user).getResult();
        assertThat("Pre Condition for the test: at least one notification must have null lastReadDate",
                notificationsPreChange.stream().anyMatch((x) -> x.getLastReadDate() == null));

        // validate as a precondition that at least one has a lastReadDate
        // defined
        UserNotification lastReadNotification = notificationsPreChange.stream()
                .filter((x) -> x.getLastReadDate() != null).max(Comparator.comparing((x) -> x.getLastReadDate())).get();
        assertThat(lastReadNotification, notNullValue());
        Instant mostRecentReadDateBeforeMarkingAllRead = lastReadNotification.getLastReadDate();
        assertThat(mostRecentReadDateBeforeMarkingAllRead, lessThan(Instant.now()));

        // marks all read for the user
        ServiceResponse<Boolean> markedResponse = userNotificationService.markAllRead(user);
        assertThat(markedResponse, isResponseOk());

        List<UserNotification> notificationsPostChange = userNotificationService.findAll(user).getResult();

        assertThat("After marking allRead, all must have lastReadDate filled in",
                notificationsPostChange.stream().noneMatch((x) -> x.getLastReadDate() == null));

        // ensure that they were updated
        Instant mostRecentReadDateAfterMarkingAllRead = notificationsPostChange.stream()
                .max(Comparator.comparing((x) -> x.getLastReadDate())).get().getLastReadDate();
        assertThat(mostRecentReadDateAfterMarkingAllRead, greaterThan(mostRecentReadDateBeforeMarkingAllRead));

        // ensure that the notification that was already marked as read was not
        // updated
        assertThat(mostRecentReadDateBeforeMarkingAllRead, equalTo(userNotificationService
                .getUserNotification(user, lastReadNotification.getUuid()).getResult().getLastReadDate()));

    }

    // ======================= markRead ========================= //
    @Test
    public void shouldMarkReadWhenUnread() {
        // validate precondition: notification was unread before changes
        assertThat("Pre condition: should be unread before test", isUnreadNotification.test(userNotificationService
                .getUserNotification(user, SCHEDULED_MAINTENANCE_NOTIFICATION_UUID).getResult()));

        // call the markRead service and check its result
        ServiceResponse<UserNotification> markReadResp = userNotificationService.markRead(user,
                SCHEDULED_MAINTENANCE_NOTIFICATION_UUID);
        assertThat(markReadResp, isResponseOk());
        UserNotification afterMarkedRead = markReadResp.getResult();

        assertThat("Should be marked as read", isReadNotification.test(afterMarkedRead));
        assertThat("Should be marked read in the past, not in the future", afterMarkedRead.getLastReadDate(),
                lessThanOrEqualTo(Instant.now()));
        assertThat("Should be marked read in the recent past, not in the distant past",
                afterMarkedRead.getLastReadDate(), greaterThan(Instant.now().minusMillis(10000)));
        assertThat("Should return the same notification", afterMarkedRead.getUuid(),
                equalTo(SCHEDULED_MAINTENANCE_NOTIFICATION_UUID));

        // validate if we do search, we should get the updated notification
        UserNotification afterSearched = userNotificationService
                .getUserNotification(user, SCHEDULED_MAINTENANCE_NOTIFICATION_UUID).getResult();

        assertThat("Should be marked as read", isReadNotification.test(afterSearched));
        assertThat("Should be marked read in the past, not in the future", afterSearched.getLastReadDate(),
                lessThanOrEqualTo(Instant.now()));
        assertThat("Should be marked read in the recent past, not in the distant past", afterSearched.getLastReadDate(),
                greaterThan(Instant.now().minusMillis(10000)));
        assertThat(afterSearched.getUuid(), equalTo(SCHEDULED_MAINTENANCE_NOTIFICATION_UUID));

        // should be idempotent - a second markRead should not update the
        // lastReadDate
        Instant lastReadDate = afterMarkedRead.getLastReadDate();
        ServiceResponse<UserNotification> markReadAgainResp = userNotificationService.markRead(user,
                SCHEDULED_MAINTENANCE_NOTIFICATION_UUID);
        assertThat(markReadAgainResp, isResponseOk());
        UserNotification afterMarkedAgainRead = markReadAgainResp.getResult();
        assertThat(lastReadDate, equalTo(afterMarkedAgainRead.getLastReadDate()));
        assertThat("Should be marked read", isReadNotification.test(afterMarkedAgainRead));
    }

    @Test
    public void shouldMarkReadWhenReadButNotUpdateLastReadDate() {
        // validate precondition: notification was unread before changes
        UserNotification beforeMarkingRead = userNotificationService
                .getUserNotification(user, PAYMENT_PENDING_NOTIFICATION_UUID).getResult();
        assertThat("Pre condition: should be read before test", isReadNotification.test(beforeMarkingRead));
        Instant beforeMarkingReadLastReadDate = beforeMarkingRead.getLastReadDate();

        // call the markRead service and check its result
        ServiceResponse<UserNotification> markReadResp = userNotificationService.markRead(user,
                PAYMENT_PENDING_NOTIFICATION_UUID);
        assertThat(markReadResp, isResponseOk());
        UserNotification afterMarkedRead = markReadResp.getResult();

        assertThat("Should be marked as read", isReadNotification.test(afterMarkedRead));
        assertThat("Should not update the last read date", afterMarkedRead.getLastReadDate(),
                equalTo(beforeMarkingReadLastReadDate));
        assertThat("Should return the same notification", afterMarkedRead.getUuid(),
                equalTo(PAYMENT_PENDING_NOTIFICATION_UUID));
    }

    @Test
    public void shouldFailWhenMarkReadOfDifferentUser() {
        ServiceResponse<UserNotification> markReadResp = userNotificationService.markRead(anotherUser,
                SCHEDULED_MAINTENANCE_NOTIFICATION_UUID);
        assertThat(markReadResp, hasErrorMessage(Validations.USER_NOTIFICATION_NOT_FOUND.getCode()));
    }

    @Test
    public void shouldFailWhenMarkReadWithNullUser() {
        ServiceResponse<UserNotification> markReadResp = userNotificationService.markRead(null,
                PAYMENT_PENDING_NOTIFICATION_UUID);
        assertThat(markReadResp, hasErrorMessage(Validations.USER_NULL.getCode()));
    }

    @Test
    public void shouldFailWhenMarkReadWithNullUUID() {
        ServiceResponse<UserNotification> markReadResp = userNotificationService.markRead(user, null);
        assertThat(markReadResp, hasErrorMessage(Validations.USER_NOTIFICATION_NULL_UUID.getCode()));
    }

    // ======================= markUnRead ========================= //
    @Test
    public void shouldMarkUnreadWhenRead() {
        // validate precondition: notification was unread before changes
        assertThat("Pre condition: should be read before test", isReadNotification.test(userNotificationService
                .getUserNotification(user, PAYMENT_PENDING_NOTIFICATION_UUID).getResult()));

        // call the markUnread service and check its result
        ServiceResponse<UserNotification> markUnreadResp = userNotificationService.markUnread(user,
                PAYMENT_PENDING_NOTIFICATION_UUID);
        assertThat(markUnreadResp, isResponseOk());
        UserNotification afterMarkedUnread = markUnreadResp.getResult();

        assertThat("Should be marked as unread", isUnreadNotification.test(afterMarkedUnread));
        assertThat("Should return the same notification", afterMarkedUnread.getUuid(),
                equalTo(PAYMENT_PENDING_NOTIFICATION_UUID));

        // validate if we do search, we should get the updated notification
        UserNotification afterSearched = userNotificationService
                .getUserNotification(user, PAYMENT_PENDING_NOTIFICATION_UUID).getResult();
        assertThat("Should be marked as unread", isUnreadNotification.test(afterSearched));

        // should be idempotent - a second markUnread should not toggle update the
        ServiceResponse<UserNotification> markUnreadAgainResp = userNotificationService.markUnread(user,
                PAYMENT_PENDING_NOTIFICATION_UUID);
        assertThat(markUnreadAgainResp, isResponseOk());
        UserNotification afterMarkedAgainUnread = markUnreadAgainResp.getResult();
        assertThat("Should be marked as unread", isUnreadNotification.test(afterMarkedAgainUnread));
    }


    @Test
    public void shouldFailWhenMarkUnreadOfDifferentUser() {
        ServiceResponse<UserNotification> markReadResp = userNotificationService.markUnread(anotherUser,
                SCHEDULED_MAINTENANCE_NOTIFICATION_UUID);
        assertThat(markReadResp, hasErrorMessage(Validations.USER_NOTIFICATION_NOT_FOUND.getCode()));
    }

    @Test
    public void shouldFailWhenMarkUnreadWithNullUser() {
        ServiceResponse<UserNotification> markReadResp = userNotificationService.markUnread(null,
                PAYMENT_PENDING_NOTIFICATION_UUID);
        assertThat(markReadResp, hasErrorMessage(Validations.USER_NULL.getCode()));
    }

    @Test
    public void shouldFailWhenMarkUnReadWithNullUUID() {
        ServiceResponse<UserNotification> markReadResp = userNotificationService.markUnread(user, null);
        assertThat(markReadResp, hasErrorMessage(Validations.USER_NOTIFICATION_NULL_UUID.getCode()));
    }

    
    // ======================= post ========================= //
    @Test
    public void shouldPostWhenCallPost() {
        assertThat("x", not(userNotificationService.hasNewNotifications(user).getResult()));

        
        UserNotification newNotification = UserNotification.builder().subject("My Subject").body("My Body").correlationUuid(POSTED_CORRELATION_UUID).build();
        ServiceResponse<UserNotification> resp = userNotificationService.postNotification(user, newNotification);
        assertThat(resp, isResponseOk());
        
        assertThat(resp.getResult().getUuid(), notNullValue());
        assertThat(resp.getResult().getCorrelationUuid(), equalTo(POSTED_CORRELATION_UUID));
        
        assertThat("x", userNotificationService.hasNewNotifications(user).getResult());
        assertThat(userNotificationService.unmarkHasNewNotifications(user), isResponseOk());
        assertThat("x", not(userNotificationService.hasNewNotifications(user).getResult()));
        
    }

}