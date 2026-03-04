package com.dev.auth.security;

import com.dev.user.model.User;
import com.dev.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Configuration
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

@Override
public UserDetails loadUserByUsername(String username) {

    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Not found"));

    return org.springframework.security.core.userdetails.User
            .builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities( new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            .build();
}
}