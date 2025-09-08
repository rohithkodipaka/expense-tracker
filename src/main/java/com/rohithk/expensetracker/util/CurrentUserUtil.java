package com.rohithk.expensetracker.util;


import com.rohithk.expensetracker.entity.User;
import com.rohithk.expensetracker.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserUtil {

    private final UserRepository userRepository;

    public CurrentUserUtil(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public UUID getId(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()){
            throw new IllegalStateException("No authenticated user");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(()-> new IllegalStateException("Authenticated User Not Found In DB"));
    }

    public User getUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated())
            throw new IllegalStateException("No authenticated user");
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB"));
    }
}
