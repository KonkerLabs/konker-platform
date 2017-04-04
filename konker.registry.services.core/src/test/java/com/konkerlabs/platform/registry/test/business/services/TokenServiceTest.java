package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TokenService;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Duration;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        BusinessTestConfiguration.class
})
@UsingDataSet(locations = {
        "/fixtures/tenants.json",
        "/fixtures/users.json",
        "/fixtures/passwordBlacklist.json"
})
public class TokenServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private UserRepository _userRepository;
    @Autowired
    private TokenService _tokenService;

    private User _user;

    @Before
    public void setUp() throws Exception {
        _user = _userRepository.findOne("admin@konkerlabs.com");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldReturnValidToken(){
        ServiceResponse<String> response = _tokenService.generateToken(
                TokenService.Purpose.RESET_PASSWORD, _user, Duration.ofSeconds(10));
        Assert.assertTrue(_tokenService.getToken(response.getResult()).isOk());
    }

    @Test
    public void shouldNotReturnValidToken(){
        Assert.assertTrue(_tokenService.getToken(null).getStatus() == ServiceResponse.Status.ERROR);
        Assert.assertTrue(_tokenService.getToken("").getStatus() == ServiceResponse.Status.ERROR);
    }

    @Test
    public void shouldGenerateValidToken(){
       ServiceResponse<String> response = _tokenService.generateToken(
               TokenService.Purpose.RESET_PASSWORD, _user, Duration.ofSeconds(10));
       Assert.assertTrue(response.isOk());
       Assert.assertFalse(response.getResult().isEmpty());
    }

    @Test
    public void shouldNotGenerateValidToken(){
        ServiceResponse<String> response = _tokenService.generateToken(
                TokenService.Purpose.RESET_PASSWORD, null, Duration.ofSeconds(10));
        Assert.assertTrue(response.getStatus() == ServiceResponse.Status.ERROR);

        response = _tokenService.generateToken(
                TokenService.Purpose.RESET_PASSWORD, _user, null);
        Assert.assertTrue(response.getStatus() == ServiceResponse.Status.ERROR);

        _user.setEmail("test@test");
        response = _tokenService.generateToken(
                TokenService.Purpose.RESET_PASSWORD, _user, Duration.ofSeconds(10));
        Assert.assertTrue(response.getStatus() == ServiceResponse.Status.ERROR);

        response = _tokenService.generateToken(
                null, _user, Duration.ofSeconds(10));
        Assert.assertTrue(response.getStatus() == ServiceResponse.Status.ERROR);
    }

    @Test
    public void shouldBeValidToken(){
        ServiceResponse<String> response = _tokenService.generateToken(
                TokenService.Purpose.RESET_PASSWORD, _user, Duration.ofDays(1));
        Assert.assertTrue(_tokenService.isValidToken(response.getResult()).isOk());
    }

    @Test
    public void shouldBeInvalidToken(){
        ServiceResponse<String> response = _tokenService.generateToken(
                TokenService.Purpose.RESET_PASSWORD, _user, Duration.ofSeconds((-1)));
        Assert.assertTrue(_tokenService.isValidToken(response.getResult()).getStatus() == ServiceResponse.Status.ERROR);

        response = _tokenService.generateToken(
                TokenService.Purpose.RESET_PASSWORD, _user, Duration.ofDays(1));
        _tokenService.invalidateToken(response.getResult());
        Assert.assertTrue(_tokenService.isValidToken(response.getResult()).getStatus() == ServiceResponse.Status.ERROR);

        Assert.assertTrue(_tokenService.isValidToken(null).getStatus() == ServiceResponse.Status.ERROR);
    }

    @Test
    public void shouldBeInvalidatedToken()
    {
        ServiceResponse<String> response = _tokenService.generateToken(
                TokenService.Purpose.RESET_PASSWORD, _user, Duration.ofDays(1));
        Assert.assertTrue(_tokenService.invalidateToken(response.getResult()).isOk());

        Assert.assertTrue(_tokenService.invalidateToken(null).getStatus() == ServiceResponse.Status.ERROR);

        Assert.assertTrue(_tokenService.invalidateToken(
                UUID.randomUUID().toString()).getStatus() == ServiceResponse.Status.ERROR);

        response = _tokenService.generateToken(
                TokenService.Purpose.RESET_PASSWORD, _user, Duration.ofDays(1));
        _tokenService.invalidateToken(response.getResult());
        Assert.assertTrue(_tokenService.invalidateToken(response.getResult()).getStatus() == ServiceResponse.Status.ERROR);
    }
}
