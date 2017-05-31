package com.konkerlabs.platform.registry.web.controllers;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.UserNotificationService;

@Controller
@RequestMapping("/notification-status")
@Scope(scopeName = "request")
public class UserNotificationStatusController {

    @Autowired
    private User user;
    
    @Autowired
    private UserNotificationService userNotificationService;
    
    /**
     * Gets the current notification status. Do we have new notifications?
     * 
     * The response is a JSON containing a single key and a boolean: hasNewNotifications
     * @param principal
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody Map<String, Object> hasNewNotitifications(
            Principal principal) {

        ServiceResponse<Boolean> response = userNotificationService.hasNewNotifications(user);
        Boolean b = Optional.ofNullable(response.getResult()).orElse(Boolean.FALSE);
        return Collections.singletonMap("hasNewNotifications", b);
    }
    
    /**
     * Sets the notification status with hasNewNotifications = FALSE. Ignores otherwise.
     * 
     * The response is a JSON containing a single key and a boolean: hasNewNotifications
     * @param principal
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json", consumes="application/json")
    public @ResponseBody Map<String, Object> unmarkNewNotitifications(@RequestBody Map<String, Object> body,
            Principal principal) {

        if (Boolean.FALSE.equals(body.getOrDefault("hasNewNotifications", Boolean.TRUE))) {
            userNotificationService.unmarkHasNewNotifications(user);
        } 
        
        ServiceResponse<Boolean> response = userNotificationService.hasNewNotifications(user);
        Boolean b = Optional.ofNullable(response.getResult()).orElse(Boolean.FALSE);
        return Collections.singletonMap("hasNewNotifications", b);
    }
}
