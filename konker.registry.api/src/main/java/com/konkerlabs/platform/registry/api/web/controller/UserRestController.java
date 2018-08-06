package com.konkerlabs.platform.registry.api.web.controller;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.konkerlabs.platform.registry.business.model.OauthClientDetails;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.konkerlabs.platform.registry.business.model.Role;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.enumerations.DateFormat;
import com.konkerlabs.platform.registry.business.model.enumerations.Language;
import com.konkerlabs.platform.registry.business.model.enumerations.TimeZone;
import com.konkerlabs.platform.registry.business.services.api.RoleService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.UserService;
import com.konkerlabs.platform.registry.business.services.api.UserService.Validations;
import com.konkerlabs.platform.registry.business.services.api.UserService.Errors;

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
    private RoleService roleService;

    @Autowired
    private OauthClientDetails user;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_USER')")
    @ApiOperation(
            value = "List all users by application",
            response = UserVO.class)
    public List<UserVO> list() throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<List<User>> userResponse = userService.findAll(tenant);

        if (!userResponse.isOk()) {
            throw new BadServiceResponseException( userResponse, validationsCode);
        } else {
            return new UserVO().apply(userResponse.getResult());
        }

    }

    @GetMapping(path = "/{email:.+}")
    @PreAuthorize("hasAuthority('SHOW_USER')")
    @ApiOperation(
            value = "Get a user by email",
            response = RestResponse.class
    )
    public UserVO read(@PathVariable("email") String email) throws NotFoundResponseException {

        Tenant tenant = user.getTenant();

        ServiceResponse<User> userResponse = userService.findByTenantAndEmail(tenant, email);

        if (!userResponse.isOk()) {
            throw new NotFoundResponseException(userResponse);
        } else {
            return new UserVO().apply(userResponse.getResult());
        }

    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADD_USER')")
    @ApiOperation(value = "Create a user")
    public UserVO create(
            @ApiParam(name = "body", value = "JSON filled with the fields described in Model and Example Value beside", required = true)
            @RequestBody UserVO userForm) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();
        Role role = roleService.findByName(RoleService.ROLE_IOT_USER).getResult();

        User userFromForm = User.builder()
        		.email(userForm.getEmail())
        		.password(userForm.getPassword())
        		.phone(userForm.getPhone())
                .name(userForm.getName())
                .notificationViaEmail(userForm.isNotificationViaEmail())
                .tenant(tenant)
                .dateFormat(DateFormat.YYYYMMDD)
                .zoneId(TimeZone.AMERICA_SAO_PAULO)
                .language(Language.PT_BR)
                .roles(Collections.singletonList(role))
                .build();
        
        String password = null;
        if (Optional.ofNullable(userForm.getPassword()).isPresent() && !userForm.getPassword().isEmpty()) {
        	password = userForm.getPassword();
        }

        ServiceResponse<User> userResponse = userService.save(userFromForm, password, password);

        if (!userResponse.isOk()) {
            throw new BadServiceResponseException( userResponse, validationsCode);
        } else {
            return new UserVO().apply(userResponse.getResult());
        }

    }

    @PutMapping(path = "/{email:.+}")
    @PreAuthorize("hasAuthority('EDIT_USER')")
    @ApiOperation(value = "Update a user")
    public void update(
            @PathVariable("email") String email,
            @ApiParam(name = "body", value = "JSON filled with the fields described in Model and Example Value beside", required = true)
            @RequestBody UserInputVO userForm) throws BadServiceResponseException {

        Tenant tenant = user.getTenant();

        User userFromDB = null;
        ServiceResponse<User> userResponse = userService.findByTenantAndEmail(tenant, email);

        if (!userResponse.isOk()) {
            throw new BadServiceResponseException( userResponse, validationsCode);
        } else {
            userFromDB = userResponse.getResult();
        }

        // update fields
        String password = "";
        if (Optional.ofNullable(userForm.getPassword()).isPresent() && !userForm.getPassword().isEmpty()) {
        	userFromDB.setPassword(userForm.getPassword());
        	password = userForm.getPassword();
        }
        userFromDB.setPhone(userForm.getPhone());
        userFromDB.setName(userForm.getName());
        userFromDB.setNotificationViaEmail(userForm.isNotificationViaEmail());

        ServiceResponse<User> updateResponse = userService.save(userFromDB, password, password);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException( userResponse, validationsCode);

        }

    }

    @Override
    public void afterPropertiesSet() {

        for (Validations value : UserService.Validations.values()) {
            validationsCode.add(value.getCode());
        }
        
        for (Errors value : UserService.Errors.values()) {
            validationsCode.add(value.getCode());
        }

    }

}
