package com.interviewagent.interviewagent_backend.security;


import com.interviewagent.interviewagent_backend.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter{
    //OncePerRequest ensures that it is run once per request and not multiple times in chain

    private final JwtService jwtService;//to validate and extract info from the token
    private final CustomUserDetailsService customUserDetailsService; //to load UserDetails


    @Override
    //spring calls this function per http request
    // get the access of incoming request and response
    // filterChain lets you pass the request to the next filter
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization"); // grab the autherization header from request
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
                //create authentication token using userDetails, no credentials and user roles
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                //attaches request -related details to the authToken
                authToken.setDetails(new WebAuthenticationDetailsSource());
                // sets the authenticated user to spring security context ,so spring knows who is making the request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);// let the request continue down the filter chain
    }
}