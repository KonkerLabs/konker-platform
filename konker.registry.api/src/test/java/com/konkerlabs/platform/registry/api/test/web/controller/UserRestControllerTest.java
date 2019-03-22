package com.konkerlabs.platform.registry.api.test.web.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.konkerlabs.platform.registry.business.model.*;
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

import com.konkerlabs.platform.registry.api.config.WebMvcConfig;
import com.konkerlabs.platform.registry.api.model.UserVO;
import com.konkerlabs.platform.registry.api.test.config.MongoTestConfig;
import com.konkerlabs.platform.registry.api.test.config.WebTestConfiguration;
import com.konkerlabs.platform.registry.api.web.controller.UserRestController;
import com.konkerlabs.platform.registry.api.web.wrapper.CrudResponseAdvice;
import com.konkerlabs.platform.registry.business.services.api.RoleService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = UserRestController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {
        WebTestConfiguration.class,
        MongoTestConfig.class,
        WebMvcConfig.class,
        CrudResponseAdvice.class
})
public class UserRestControllerTest extends WebLayerTestContext {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private Tenant tenant;

    @Autowired
    private OauthClientDetails loggedUser;

	private final String BASEPATH = "users";

    private User user1;

    private User user2;

    private Role role;

    @Before
    public void setUp() {
        user1 = User.builder()
        			.email("fakeuser1@domain.com")
        			.name("Fake User Hum")
        			.password("pass123")
        			.phone("99998888")
        			.notificationViaEmail(true)
        			.build();

        user2 = User.builder()
	        		.email("fakeuser2@domain.com")
	    			.name("Fake User Dois")
	    			.password("pass321")
	    			.phone("99997777")
	    			.notificationViaEmail(false)
	    			.build();

        role = Role.builder()
        			.id("1")
        			.name("ROLE_IOT_USER")
        			.build();
    }

    @After
    public void tearDown() {
        Mockito.reset(userService);
    }

    @Test
    public void shouldListUsers() throws Exception {
        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        when(userService.findAll(tenant))
                .thenReturn(ServiceResponseBuilder.<List<User>>ok().withResult(users).build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/users/")
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
        .andExpect(jsonPath("$.result", hasSize(2)))
        .andExpect(jsonPath("$.result[0].password", isEmptyOrNullString()))
        .andExpect(jsonPath("$.result[0].phone", is("99998888")))
        .andExpect(jsonPath("$.result[0].name", is("Fake User Hum")))
        .andExpect(jsonPath("$.result[0].notificationViaEmail", is(true)))
        .andExpect(jsonPath("$.result[0].email", is("fakeuser1@domain.com")))
        .andExpect(jsonPath("$.result[1].password", isEmptyOrNullString()))
        .andExpect(jsonPath("$.result[1].phone", is("99997777")))
        .andExpect(jsonPath("$.result[1].name", is("Fake User Dois")))
        .andExpect(jsonPath("$.result[1].notificationViaEmail", is(false)))
        .andExpect(jsonPath("$.result[1].email", is("fakeuser2@domain.com")));

    }

    @Test
    public void shouldTryListUsersWithInternalError() throws Exception {
        when(userService.findAll(tenant))
                .thenReturn(ServiceResponseBuilder.<List<User>>error().build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/users/")
        		.accept(MediaType.APPLICATION_JSON)
        		.contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is5xxServerError())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
        .andExpect(jsonPath("$.status", is("error")))
        .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
        .andExpect(jsonPath("$.messages").doesNotExist())
        .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldReadUser() throws Exception {

        when(userService.findByTenantAndEmail(tenant, user1.getEmail()))
                .thenReturn(ServiceResponseBuilder.<User>ok().withResult(user1).build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/users/" + user1.getEmail())
        		.contentType("application/json")
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
        .andExpect(jsonPath("$.result").isMap())
        .andExpect(jsonPath("$.result.password", isEmptyOrNullString()))
        .andExpect(jsonPath("$.result.phone", is("99998888")))
        .andExpect(jsonPath("$.result.name", is("Fake User Hum")))
        .andExpect(jsonPath("$.result.notificationViaEmail", is(true)))
        .andExpect(jsonPath("$.result.email", is("fakeuser1@domain.com")));

    }

    @Test
    public void shouldTryReadUserWithBadRequest() throws Exception {

        when(userService.findByTenantAndEmail(tenant, user1.getEmail()))
                .thenReturn(ServiceResponseBuilder.<User>error().withMessage(UserService.Validations.NO_EXIST_USER.getCode()).build());

        getMockMvc().perform(MockMvcRequestBuilders.get("/users/" + user1.getEmail())
        		.accept(MediaType.APPLICATION_JSON)
        		.contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
        .andExpect(jsonPath("$.status", is("error")))
        .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
        .andExpect(jsonPath("$.messages[0]", is("User not exist")))
        .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldCreateUser() throws Exception {

    	when(userService.save(
				org.mockito.Matchers.anyString(),
				org.mockito.Matchers.anyString(),
    			org.mockito.Matchers.any(User.class),
    			org.mockito.Matchers.anyString(),
    			org.mockito.Matchers.anyString()))
    	.thenReturn(ServiceResponseBuilder.<User>ok().withResult(user1).build());

    	when(roleService.findByName(org.mockito.Matchers.anyString()))
    		.thenReturn(ServiceResponseBuilder.<Role>ok().withResult(role).build());

    	getMockMvc().perform(MockMvcRequestBuilders.post("/users/")
    			.content(getJson(new UserVO().apply(user1)))
    			.contentType("application/json")
    			.accept(MediaType.APPLICATION_JSON))
    	.andExpect(status().is2xxSuccessful())
    	.andExpect(content().contentType("application/json;charset=UTF-8"))
    	.andExpect(jsonPath("$.code", is(HttpStatus.CREATED.value())))
    	.andExpect(jsonPath("$.status", is("success")))
    	.andExpect(jsonPath("$.timestamp",greaterThan(1400000000)))
    	.andExpect(jsonPath("$.result").isMap())
    	.andExpect(jsonPath("$.result.password", isEmptyOrNullString()))
        .andExpect(jsonPath("$.result.phone", is("99998888")))
        .andExpect(jsonPath("$.result.name", is("Fake User Hum")))
        .andExpect(jsonPath("$.result.notificationViaEmail", is(true)))
        .andExpect(jsonPath("$.result.email", is("fakeuser1@domain.com")));

    }

    @Test
    public void shouldTryCreateUserWithBadRequest() throws Exception {

    	when(userService.save(
                org.mockito.Matchers.anyString(),
                org.mockito.Matchers.anyString(),
    			org.mockito.Matchers.any(User.class),
    			org.mockito.Matchers.anyString(),
    			org.mockito.Matchers.anyString()))
    	.thenReturn(ServiceResponseBuilder.<User>error().withMessage(UserService.Validations.NO_EXIST_USER.getCode()).build());

    	getMockMvc().perform(MockMvcRequestBuilders.post("/users/")
    			.content(getJson(new UserVO().apply(user1)))
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


    @Test
    public void shouldUpdateUser() throws Exception {

        when(userService.findByTenantAndEmail(tenant, user1.getEmail()))
                .thenReturn(ServiceResponseBuilder.<User>ok().withResult(user1).build());

        when(userService.save(
				org.mockito.Matchers.anyString(),
				org.mockito.Matchers.anyString(),
        		org.mockito.Matchers.any(User.class),
        		org.mockito.Matchers.anyString(),
        		org.mockito.Matchers.anyString()))
        .thenReturn(ServiceResponseBuilder.<User>ok().withResult(user1).build());

        getMockMvc().perform(MockMvcRequestBuilders.put("/users/" + user1.getEmail())
        		.content(getJson(new UserVO().apply(user1)))
        		.contentType(MediaType.APPLICATION_JSON)
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful())
        .andExpect(jsonPath("$.code", is(HttpStatus.OK.value())))
        .andExpect(jsonPath("$.status", is("success")))
        .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
        .andExpect(jsonPath("$.result").doesNotExist());

    }

    @Test
    public void shouldTryUpdateUserWithInternalError() throws Exception {

        when(userService.findByTenantAndEmail(tenant, user1.getEmail()))
                .thenReturn(ServiceResponseBuilder.<User>ok().withResult(user1).build());

        when(userService.save(
				org.mockito.Matchers.anyString(),
				org.mockito.Matchers.anyString(),
        		org.mockito.Matchers.any(User.class),
        		org.mockito.Matchers.anyString(),
        		org.mockito.Matchers.anyString()))
        .thenReturn(ServiceResponseBuilder.<User>error().build());

        getMockMvc().perform(MockMvcRequestBuilders.put("/users/" + user1.getEmail())
        		.content(getJson(new UserVO().apply(user1)))
        		.contentType(MediaType.APPLICATION_JSON)
        		.accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is5xxServerError())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.code", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
        .andExpect(jsonPath("$.status", is("error")))
        .andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
        .andExpect(jsonPath("$.messages").doesNotExist())
        .andExpect(jsonPath("$.result").doesNotExist());

    }

	@Test
	public void shouldDeleteDevice() throws Exception {
		when(userService.remove(tenant, loggedUser.getParentUser(), user2.getEmail()))
				.thenReturn(ServiceResponseBuilder.<User>ok().build());

		getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}", BASEPATH, user2.getEmail()))
				.contentType("application/json")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.code", is(HttpStatus.NO_CONTENT.value())))
				.andExpect(jsonPath("$.status", is("success")))
				.andExpect(jsonPath("$.timestamp", greaterThan(1400000000)))
				.andExpect(jsonPath("$.result").doesNotExist());
	}

    @Test
    public void shouldTryDeleteDeviceWithIternalError() throws Exception {
        when(userService.remove(tenant, loggedUser.getParentUser(), user2.getEmail()))
                .thenReturn(ServiceResponseBuilder.<User>error()
                        .withMessage(UserService.Validations.NO_EXIST_USER.getCode())
                        .build());

        getMockMvc().perform(MockMvcRequestBuilders.delete(MessageFormat.format("/{0}/{1}", BASEPATH, user2.getEmail()))
                .contentType("application/json")
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
