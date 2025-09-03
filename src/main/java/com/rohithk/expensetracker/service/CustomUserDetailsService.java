package com.rohithk.expensetracker.service;

import com.rohithk.expensetracker.entity.User;
import com.rohithk.expensetracker.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            User u = userRepository.findByEmail(username)
                    .orElseThrow(()-> new UsernameNotFoundException("User not found"));
            return org.springframework.security.core.userdetails.User.builder()
                    .username(u.getEmail())
                    .password(u.getPassword())
                    .disabled(!u.isEnabled())
                    .authorities(u.getRoles().stream()
                            .map(k->new SimpleGrantedAuthority(k.getName()))
                            .collect(Collectors.toSet()))
                    .build();

    }
}
