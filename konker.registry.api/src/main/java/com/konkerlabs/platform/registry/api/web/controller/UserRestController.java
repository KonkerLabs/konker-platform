package com.konkerlabs.platform.registry.api.web.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.api.model.UserInputVO;
import com.konkerlabs.platform.registry.api.model.UserVO;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.business.services.api.UserService.Validations;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Scope("request")
@RequestMapping(
        value = "/users"
)
@Api(tags = "users")
public class UserRestController implements InitializingBean {

    @Autowired
    private UserService userService;

    @Autowired
    private User user;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    @ApiOperation(
            value = "List all users by organization",
            response = UserVO.class)
    public List<UserVO> list() throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<List<User>> userResponse = userService.findAll(tenant);

        if (!userResponse.isOk()) {
            throw new BadServiceResponseException(user, userResponse, validationsCode);
        } else {
            return new UserVO().apply(userResponse.getResult());
        }

    }

    @GetMapping(path = "/{email}")
    @ApiOperation(
            value = "Get a user by guid",
            response = RestResponse.class
    )
    public UserVO read(@PathVariable("email") String email) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<User> userResponse = userService.findByTenantAndEmail(tenant, email);

        if (!userResponse.isOk()) {
            throw new NotFoundResponseException(user, userResponse);
        } else {
            return new UserVO().apply(userResponse.getResult());
        }

    }

    @PostMapping
    @ApiOperation(value = "Create a user")
    public UserVO create(
            @ApiParam(name = "body", required = true)
            @RequestBody UserVO userForm) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        User user = User.builder()
        		.email(userForm.getEmail())
        		.password(userForm.getPassword())
        		.phone(userForm.getPhone())
                .name(userForm.getName())
                .notificationViaEmail(userForm.isNotificationViaEmail())
                .tenant(tenant)
                .build();

        ServiceResponse<User> userResponse = userService.save(user, user.getPassword(), user.getPassword());

        if (!userResponse.isOk()) {
            throw new BadServiceResponseException(user, userResponse, validationsCode);
        } else {
            return new UserVO().apply(userResponse.getResult());
        }

    }

    @PutMapping(path = "/{email}")
    @ApiOperation(value = "Update a user")
    public void update(
            @PathVariable("email") String email,
            @ApiParam(name = "body", required = true)
            @RequestBody UserInputVO userForm) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        User userFromDB = null;
        ServiceResponse<User> userResponse = userService.findByTenantAndEmail(tenant, email);

        if (!userResponse.isOk()) {
            throw new BadServiceResponseException(user, userResponse, validationsCode);
        } else {
            userFromDB = userResponse.getResult();
        }

        // update fields
        userFromDB.setPassword(userForm.getPassword());
        userFromDB.setPhone(userForm.getPhone());
        userFromDB.setName(userForm.getName());
        userFromDB.setNotificationViaEmail(userForm.isNotificationViaEmail());

        ServiceResponse<User> updateResponse = userService.save(userFromDB, userFromDB.getPassword(), userFromDB.getPassword());

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException(user, userResponse, validationsCode);

        }

    }

//    @DeleteMapping(path = "/{email}")
//    @ApiOperation(value = "Delete a user")
//    public void delete(@PathVariable("email") String email) throws BadServiceResponseException, NotFoundResponseException {
//
//        Tenant tenant = user.getTenant();
//
//        ServiceResponse<User> userResponse = userService.remove(tenant, email);
//
//        if (!userResponse.isOk()) {
//            if (userResponse.getResponseMessages().containsKey(Validations.NO_EXIST_USER.getCode())) {
//                throw new NotFoundResponseException(user, userResponse);
//            } else {
//                throw new BadServiceResponseException(user, userResponse, validationsCode);
//            }
//        }
//
//    }

    @Override
    public void afterPropertiesSet() throws Exception {

        for (Validations value : UserService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

    }

}
