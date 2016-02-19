package com.konkerlabs.platform.registry.security;

import com.konkerlabs.platform.registry.business.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MongoUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return Optional
            .ofNullable(userRepository.findOne(email))
            .orElseThrow(() -> new UsernameNotFoundException("authentication.credentials.invalid"));
    }
}
