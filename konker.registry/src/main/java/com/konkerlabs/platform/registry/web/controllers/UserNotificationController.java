package com.konkerlabs.platform.registry.web.controllers;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.model.UserNotification;
import com.konkerlabs.platform.registry.business.services.api.UserNotificationService;

@Controller
@RequestMapping("/notifications")
@Scope(scopeName = "request")
public class UserNotificationController {

    /**
     * This class is a wrapper over the user notification meant to expose 
     * only attributes related to the header of the messagem via JSON
     *  
     * @author wmariusso
     *
     */
    private static class UserNotificationHeaderDecoration {
        protected UserNotification un;
        protected User user;

        public UserNotificationHeaderDecoration(UserNotification un, User u) {
            this.un = un;
            this.user = u;
        }

        public static List<UserNotificationHeaderDecoration> decorate(List<UserNotification> unList, User user) {
            return unList.stream().map((x) -> new UserNotificationHeaderDecoration(x, user))
                    .collect(Collectors.toList());
        }

        @SuppressWarnings("unused")
        public String getUuid() {
            return un.getUuid();
        }

        @SuppressWarnings("unused")
        public String getSubject() {
            return un.getSubject();
        }

        @SuppressWarnings("unused")
        public String getDateTime() {
            return user.getDateFormat().getDateTimeFormatter()
                    .format(un.getDate().atZone(user.getZoneId().getZoneId()));
        }

        @SuppressWarnings("unused")
        public Long getEpochMillis() {
            return un.getDate().toEpochMilli();
        }

        @SuppressWarnings("unused")
        public boolean isUnread() {
            return Optional.ofNullable(un.getUnread()).orElse(Boolean.FALSE);
        }
    }

    /**
     * This is a wrapper over the UserNotification meant to expose only relevant
     * attributes via JSON
     * 
     * @author wmariusso
     *
     */
    private static class UserNotificationDecoration extends UserNotificationHeaderDecoration {
        public UserNotificationDecoration(UserNotification un, User u) {
            super(un, u);
        }

        public static UserNotificationDecoration decorate(UserNotification un, User user) {
            return new UserNotificationDecoration(un, user);
        }

        @SuppressWarnings("unused")
        public String getBody() {
            return un.getBody();
        }
    }

    @Autowired
    private User user;

    @Autowired
    private UserNotificationService userNotificationService;

    
    /**
     * This is the only method on this class that produces HTML. It delivers the HTML template used to
     * build the page. All the other methods return JSON.
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ModelAndView index() {
        return new ModelAndView("notifications/index").addObject("messageCount", userNotificationService.findAll(user).getResult().size());
    }

    
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<UserNotificationHeaderDecoration> list(
            @RequestParam(name = "unreadOnly", defaultValue = "false", required = false) Boolean unreadOnly,
            Principal principal) {

        List<UserNotification> notifications;
        if (unreadOnly) {
            notifications = userNotificationService.findUnread(user).getResult();
        } else {
            notifications = userNotificationService.findAll(user).getResult();
        }

        return UserNotificationHeaderDecoration.decorate(notifications, user);
    }

    @RequestMapping(value = "/{uuid}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody UserNotificationDecoration get(@PathVariable String uuid, Principal principal) {
        return UserNotificationDecoration.decorate(userNotificationService.getUserNotification(user, uuid).getResult(),
                user);
    }

    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json",
            consumes = "application/json")
    public @ResponseBody List<UserNotificationHeaderDecoration> post(@RequestBody Map<String, String> body) {
        if ("true".equalsIgnoreCase(body.getOrDefault("allRead", "false"))) {
            userNotificationService.markAllRead(user);
            // TODO: check error
            return UserNotificationHeaderDecoration.decorate(userNotificationService.findAll(user).getResult(), user);
        } else {
            return Collections.emptyList();
        }
    }

    @RequestMapping(value = "/{uuid}", method = RequestMethod.POST, produces = "application/json",
            consumes = "application/json")
    public @ResponseBody UserNotificationDecoration save(@RequestBody Map<String, String> body,
            @PathVariable String uuid) {
        if (body.containsKey("unread")) {
            if ("true".equalsIgnoreCase(body.getOrDefault("unread", "false"))) {
                return UserNotificationDecoration.decorate(userNotificationService.markUnread(user, uuid).getResult(),
                        user);
            } else {
                return UserNotificationDecoration.decorate(userNotificationService.markRead(user, uuid).getResult(),
                        user);
            }
        } else {
            return null;
        }
    }

}
