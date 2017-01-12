package com.konkerlabs.platform.registry.security;

import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import com.konkerlabs.platform.registry.config.SecurityConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.repositories.UserRepository;

import java.util.Optional;
import java.util.Random;

@Service("tenantUserDetails")
public class TenantUserDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantUserDetailsService.class);
    private static final int MIN_VALUE = 100;
    private static final int MAX_VALUE = 250;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Random random = new Random();
        int delay = random.nextInt(MAX_VALUE - MIN_VALUE) + MIN_VALUE;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        User user = userRepository.findOne(Optional.of(email).orElse("").trim().toLowerCase());
        if(user == null){
            LOGGER.debug("User not found",
                    User.builder().email(email).tenant(
                            Tenant.builder().name("NOT_FOUND").domainName("NOT_FOUND").build()
                    ).build().toURI());
            throw new UsernameNotFoundException("authentication.credentials.invalid");
        }
        return user;
    }
}
