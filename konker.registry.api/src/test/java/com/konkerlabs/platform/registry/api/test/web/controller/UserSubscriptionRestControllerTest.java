package com.konkerlabs.platform.registry.api.test.web.controller;

import com.konkerlabs.platform.registry.api.config.WebMvcConfig;
import com.konkerlabs.platform.registry.api.model.UserSubscriptionVO;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.UserSubscriptionRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.model.Role;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.RoleService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = UserSubscriptionRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class UserSubscriptionRestControllerTest extends WebLayerTestContext {

    @Autowired
    private UserService userService;

	@Autowired
	private RoleService roleService;

	private Role role;

	@Autowired
    private Tenant tenant;

    private UserSubscriptionVO subscriptionVO;

    private User user;

    @Before
    public void setUp() {
        subscriptionVO = new UserSubscriptionVO();
        subscriptionVO.setEmail("konker@konker.com");
        subscriptionVO.setCompany("konker");
        subscriptionVO.setName("Konker Team");
        subscriptionVO.setJobTitle(User.JobEnum.CEO.name());
        subscriptionVO.setPassword("encryptme");
        subscriptionVO.setPasswordType("PASSWORD");

        user = User.builder()
                .name(subscriptionVO.getName())
                .email(subscriptionVO.getEmail())
                .build();

		role = Role.builder()
				.id("1")
				.name("ROLE_IOT_USER")
				.build();

		when(roleService.findByName(org.mockito.Matchers.anyString()))
				.thenReturn(ServiceResponseBuilder.<Role>ok().withResult(role).build());

	}

    @After
    public void tearDown() {
        Mockito.reset(userService);
    }

    @Test
    public void shouldSubscribeUser() throws Exception {

        when(userService.createAccount(org.mockito.Matchers.any(User.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.anyString())).thenReturn(
                ServiceResponseBuilder.<User>ok().withResult(user).build()
        );

        getMockMvc()
        .perform(MockMvcRequestBuilders.post("/userSubscription")
        		.content(getJson(subscriptionVO))
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
        .andExpect(jsonPath("$.result").isMap())
        .andExpect(jsonPath("$.result.name", is("Konker Team")))
        .andExpect(jsonPath("$.result.email", is("konker@konker.com")))
        ;

    }


    @Test
    public void shouldSubscribeUserWithEmptyCompany() throws Exception {

        when(userService.createAccount(org.mockito.Matchers.any(User.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.anyString())).thenReturn(
                ServiceResponseBuilder.<User>ok().withResult(user).build()
        );

        getMockMvc()
                .perform(MockMvcRequestBuilders.post("/userSubscription")
                        .content(getJson(subscriptionVO))
                        .contentType("application/json")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result.name", is("Konker Team")))
                .andExpect(jsonPath("$.result.email", is("konker@konker.com")))
        ;

    }


    @Test
    public void shouldTrySubscribeUserWithInvalidPasswordType() throws Exception {

        when(userService.createAccount(org.mockito.Matchers.any(User.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.anyString())).thenReturn(
                ServiceResponseBuilder.<User>ok().withResult(user).build()
        );

        subscriptionVO.setPasswordType("PLAIN");

        getMockMvc()
                .perform(MockMvcRequestBuilders.post("/userSubscription")
                        .content(getJson(subscriptionVO))
                        .contentType("application/json")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").exists())
                .andExpect(jsonPath("$.result").doesNotExist());

        ;

    }


    @Test
    public void shouldTrySubscribeUserWithNullPasswordType() throws Exception {

        when(userService.createAccount(org.mockito.Matchers.any(User.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.anyString())).thenReturn(
                ServiceResponseBuilder.<User>ok().withResult(user).build()
        );

        subscriptionVO.setPasswordType(null);

        getMockMvc()
                .perform(MockMvcRequestBuilders.post("/userSubscription")
                        .content(getJson(subscriptionVO))
                        .contentType("application/json")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
                .andExpect(jsonPath("$.messages").exists())
                .andExpect(jsonPath("$.result").doesNotExist());

        ;

    }


    @Test
    public void shouldSubscribeUserWithBCrypt() throws Exception {

        when(userService.createAccountWithPasswordHash(org.mockito.Matchers.any(User.class), org.mockito.Matchers.anyString())).thenReturn(
                ServiceResponseBuilder.<User>ok().withResult(user).build()
        );

        subscriptionVO.setPasswordType("BCRYPT_HASH");

        getMockMvc()
                .perform(MockMvcRequestBuilders.post("/userSubscription")
                        .content(getJson(subscriptionVO))
                        .contentType("application/json")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result.name", is("Konker Team")))
                .andExpect(jsonPath("$.result.email", is("konker@konker.com")))
        ;

    }

    @Test
    public void shouldTrySubscribeApplicationWithBadRequest() throws Exception {

        when(userService.createAccount(org.mockito.Matchers.any(User.class), org.mockito.Matchers.anyString(), org.mockito.Matchers.anyString())).thenReturn(
                ServiceResponseBuilder.<User>error().withMessage(UserService.Validations.INVALID_PASSWORD_LENGTH.getCode()).build()
        );

        getMockMvc()
        .perform(MockMvcRequestBuilders.post("/userSubscription")
        		.content(getJson(subscriptionVO))
        		.contentType(MediaType.APPLICATION_JSON)
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
        .andExpect(jsonPath("$.status", is("error")))
        .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
        .andExpect(jsonPath("$.messages").exists())
        .andExpect(jsonPath("$.result").doesNotExist());

    }

}
