package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.base.RedisTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.hamcrest.MatcherAssert.assertThat;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class,
        RedisTestConfiguration.class
})
@UsingDataSet(locations = {
        "/fixtures/tenants.json",
        "/fixtures/users.json",
        "/fixtures/passwordBlacklist.json"
})
public class UserServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant tenant;
    private User user;
    private static final String oldPassword="abc123456789$$";
    private static final String oldPasswordWrong="password";
    private static final String newPassword="123456789abc$$";
    private static final String newPasswordWrong="123456789abc";
    private static final String newPasswordblackListed="aaaaaaaaaaaa";
    private static final String newPasswordConfirmation="123456789abc$$";
    private static final String newPasswordConfirmationWrong="abc124$$";


    @Before
    public void setUp() throws Exception {
        tenant = tenantRepository.findByDomainName("konker");
        user = userRepository.findOne("admin@konkerlabs.com");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldReturnErrorForInvalidUserName(){
        user.setName(null);
        ServiceResponse<User> serviceResponse =
                userService.save(user, oldPassword,
                        newPassword, newPasswordConfirmation);
        Assert.assertNotNull(serviceResponse);
        assertThat(
                serviceResponse,
                hasErrorMessage(UserService.Validations.INVALID_USER_NAME.getCode()));
    }

    @Test
    public void shouldReturnErrorForInvalidUserEmail(){
        user.setEmail("goWrong@noway.com");
        ServiceResponse<User> serviceResponse =
                userService.save(user, oldPassword,
                        newPassword, newPasswordConfirmation);
        Assert.assertNotNull(serviceResponse);
        assertThat(
                serviceResponse,
                hasErrorMessage(UserService.Validations.INVALID_USER_DETAILS.getCode()));
    }

    @Test
    public void shouldReturnErrorForInvalidPassword(){
        ServiceResponse<User> serviceResponse =
                userService.save(user, oldPasswordWrong,
                        newPassword, newPasswordConfirmation);
        Assert.assertNotNull(serviceResponse);
        assertThat(
                serviceResponse,
                hasErrorMessage(UserService.Validations.INVALID_PASSWORD_INVALID.getCode()));
    }

    @Test
    public void shouldReturnErrorForInvalidNewPassword(){
        ServiceResponse<User> serviceResponse =
                userService.save(user, oldPassword,
                        newPasswordWrong, newPasswordConfirmation);
        Assert.assertNotNull(serviceResponse);
        assertThat(
                serviceResponse,
                hasErrorMessage(UserService.Validations.INVALID_PASSWORD_CONFIRMATION.getCode()));
    }

    @Test
    public void shouldReturnErrorForInvalidNewPasswordConfirmation(){
        ServiceResponse<User> serviceResponse =
                userService.save(user, oldPassword,
                        newPassword, newPasswordConfirmationWrong);
        Assert.assertNotNull(serviceResponse);
        assertThat(
                serviceResponse,
                hasErrorMessage(UserService.Validations.INVALID_PASSWORD_CONFIRMATION.getCode()));
    }

    @Test
    public void shouldReturnErrorForInvalidNewPasswordBlackListed(){
        ServiceResponse<User> serviceResponse =
                userService.save(user, oldPassword,
                        newPasswordblackListed, newPasswordblackListed);
        Assert.assertNotNull(serviceResponse);
        assertThat(
                serviceResponse,
                hasErrorMessage(UserService.Validations.INVALID_PASSWORD_BLACKLISTED.getCode()));
    }

    @Test
    public void shouldReturnErrorForInvalidUserLanguage(){
        user.setLanguage(null);
        ServiceResponse<User> serviceResponse =
                userService.save(user, oldPassword,
                        newPassword, newPasswordConfirmation);
        Assert.assertNotNull(serviceResponse);
        assertThat(
                serviceResponse,
                hasErrorMessage(UserService.Validations.INVALID_USER_PREFERENCE_LANGUAGE.getCode()));
    }

    @Test
    public void shouldReturnErrorForInvalidUserLocale(){
        user.setZoneId(null);
        ServiceResponse<User> serviceResponse =
                userService.save(user, oldPassword,
                        newPassword, newPasswordConfirmation);
        Assert.assertNotNull(serviceResponse);
        assertThat(
                serviceResponse,
                hasErrorMessage(UserService.Validations.INVALID_USER_PREFERENCE_LOCALE.getCode()));
    }

    @Test
    public void shouldReturnErrorForInvalidUserDateFormat(){
        user.setDateFormat(null);
        ServiceResponse<User> serviceResponse =
                userService.save(user, oldPassword,
                        newPassword, newPasswordConfirmation);
        Assert.assertNotNull(serviceResponse);
        assertThat(
                serviceResponse,
                hasErrorMessage(UserService.Validations.INVALID_USER_PREFERENCE_DATEFORMAT.getCode()));
    }

    @Test
    public void shouldSaveNewPassword(){
        ServiceResponse<User> serviceResponse =
                userService.save(user, oldPassword,
                        newPassword, newPasswordConfirmation);
        Assert.assertNotNull(serviceResponse);
        assertThat(
                serviceResponse,
                isResponseOk());

        User updated = userRepository.findOne(user.getEmail());
        assertThat(updated.getPassword(), !equals(user.getPassword()));
    }

    @Test
    public void shouldSaveName() {
        user.setName("newName");
        ServiceResponse<User> serviceResponse =
                userService.save(user, oldPassword,
                        newPassword, newPasswordConfirmation);
        Assert.assertNotNull(serviceResponse);
        assertThat(
                serviceResponse,
                isResponseOk());

        User updated = userRepository.findOne(user.getEmail());
        assertThat(updated.getName(), !equals(user.getName()));
    }

    @Test
    public void shouldLocale() {
        user.setZoneId(TimeZone.AMERICA_LOS_ANGELES);
        ServiceResponse<User> serviceResponse =
                userService.save(user, oldPassword,
                        newPassword, newPasswordConfirmation);
        Assert.assertNotNull(serviceResponse);
        assertThat(
                serviceResponse,
                isResponseOk());

        User updated = userRepository.findOne(user.getEmail());
        assertThat(updated.getZoneId().getCode(), !equals(user.getZoneId().getCode()));
    }

    @Test
    public void shouldSaveLanguage() {
        user.setLanguage(Language.PT_BR);
        ServiceResponse<User> serviceResponse =
                userService.save(user, oldPassword,
                        newPassword, newPasswordConfirmation);
        Assert.assertNotNull(serviceResponse);
        assertThat(
                serviceResponse,
                isResponseOk());

        User updated = userRepository.findOne(user.getEmail());
        assertThat(updated.getLanguage().getCode(), !equals(user.getLanguage().getCode()));
    }

    @Test
    public void shouldSaveDateFormat() {
        user.setDateFormat(DateFormat.MMDDYYYY);
        ServiceResponse<User> serviceResponse =
                userService.save(user, oldPassword,
                        newPassword, newPasswordConfirmation);
        Assert.assertNotNull(serviceResponse);
        assertThat(
                serviceResponse,
                isResponseOk());

        User updated = userRepository.findOne(user.getEmail());
        assertThat(updated.getDateFormat().getCode(), !equals(user.getDateFormat().getCode()));
    }
    
    @Test
    public void shouldRaiseAnExceptionIfEmailNull() {
        ServiceResponse<User> serviceResponse = userService.findByEmail(null);
        
        Assert.assertNotNull(serviceResponse);
        assertThat(serviceResponse, hasErrorMessage(UserService.Validations.NO_EXIST_USER.getCode()));

    }
    
    @Test
    public void shouldReturnUser() {
        ServiceResponse<User> serviceResponse = userService.findByEmail("admin@konkerlabs.com");
        
        Assert.assertNotNull(serviceResponse);
        assertThat(serviceResponse, isResponseOk());
        Assert.assertEquals(user, serviceResponse.getResult());

    }
}
