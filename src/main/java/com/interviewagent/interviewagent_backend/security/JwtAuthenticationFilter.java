package com.interviewagent.interviewagent_backend.security;


import com.interviewagent.interviewagent_backend.service.CustomUserDetailsService;
import com.interviewagent.interviewagent_backend.service.JwtService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{
    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService customUserDetailsService) {
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }

    //OncePerRequest ensures that it is run once per request and not multiple times in chain
    @PostConstruct
    public void init() {
        System.out.println(">>> JwtAuthenticationFilter initialized");
    }

    private final JwtService jwtService;//to validate and extract info from the token
    private final CustomUserDetailsService customUserDetailsService; //to load UserDetails


    //spring calls this function per http request
    // get the access of incoming request and response
    // filterChain lets you pass the request to the next filter
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        System.out.println("JwtAuthenticationFilter triggered for: " + request.getServletPath());
        String requestPath = request.getServletPath(); // jwt is used to verify a logged user
        if(requestPath.startsWith("/api/auth")){
            System.out.println("Bypassing JWT for: " + requestPath);
            filterChain.doFilter(request,response); // signing up -> they dont have token yet, login -> they are asking token, verifying -> still unauthenticated
            return;
        }
        final String authHeader = request.getHeader("Authorization"); // grab the authorization header from request
        System.out.println("Auth header: " + authHeader);
        final String jwt;
        final String userEmail;

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            // if there is no authorization header, skip jwt processing
            filterChain.doFilter(request, response);
            // let the request proceed as usual
            return;
        }

        jwt = authHeader.substring(7);// strip the bearer prefix to get the raw jwt token
        userEmail = jwtService.extractUsername(jwt); //extract the username from the token using jwtServide

        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
            //if the username is extracted sucessfully, load the user from DB
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);

            if(jwtService.isTokenValid(jwt, userDetails.getUsername())){

                //functional interface to extract a custom claim from JWT
                String role = jwtService.extractClaim(jwt, claims -> claims.get("role", String.class));
                //create authentication token using userDetails, no credentials and user roles
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

                //attaches request -related details to the authToken
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // sets the authenticated user to spring security context ,so spring knows who is making the request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);// let the request continue down the filter chain
    }
}