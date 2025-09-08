package com.rohithk.expensetracker.controller;


import com.rohithk.expensetracker.dto.request.AuthRequest;
import com.rohithk.expensetracker.dto.request.LoginRequest;
import com.rohithk.expensetracker.dto.response.JwtResponse;
import com.rohithk.expensetracker.entity.Role;
import com.rohithk.expensetracker.entity.User;
import com.rohithk.expensetracker.repository.RoleRepository;
import com.rohithk.expensetracker.repository.UserRepository;
import com.rohithk.expensetracker.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthenticationController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationController(UserRepository userRepository,RoleRepository roleRepository,
                                    PasswordEncoder passwordEncoder,AuthenticationManager authenticationManager,
                                    JwtService jwtService){
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?>  register(@RequestBody AuthRequest authRequest){
        userRepository.findByEmail(authRequest.email()).ifPresent(user -> {
                throw new IllegalArgumentException("Email is already in use");});
        User user = new User();
        user.setEmail(authRequest.email());
        user.setPassword(passwordEncoder.encode(authRequest.password()));
        user.setFullName(authRequest.fullName());
        user.setEnabled(true);
        var userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(()->new IllegalStateException(("ROLE_USER is missing in DB")));
        user.setRoles(Set.of(userRole));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest loginRequest){
        log.info("Authentication using email and password is initiated");
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(),
                    loginRequest.password()));
        log.info("Successfully authenticated user");
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(()->new IllegalStateException(("User not found")));
        var roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        String token = jwtService.generateJwtToken(user.getEmail(),roleNames);
        return ResponseEntity.ok(new JwtResponse(token, "Bearer ", user.getEmail(), roleNames.toArray(String[]::new)));
    }

}
