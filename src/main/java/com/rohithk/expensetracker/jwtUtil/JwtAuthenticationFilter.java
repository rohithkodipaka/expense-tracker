package com.rohithk.expensetracker.jwtUtil;


import com.rohithk.expensetracker.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService){
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull FilterChain filterChain)
            throws ServletException, IOException {
            String authToken = request.getHeader(HttpHeaders.AUTHORIZATION);
            log.info("Authentication filter initiated");
            if(StringUtils.hasText(authToken) && authToken.startsWith("Bearer ")){
                String token = authToken.substring(7);
                log.info("Authentication token extracted successfully");

                try{
                    Jws<Claims> claims = jwtService.parseToken(token);
                    String email = claims.getBody().getSubject();
                    log.info("Email extracted from the token");
                    log.info("call to fetch user Details from email is initiated");
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    var authentication = new UsernamePasswordAuthenticationToken(userDetails,null,
                            userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                catch(Exception ex){
                    SecurityContextHolder.clearContext();
                    log.info("Exception occured while fetching user details from jwt Token"+ex.getMessage());
                }
            }
            filterChain.doFilter(request,response);
    }
}
