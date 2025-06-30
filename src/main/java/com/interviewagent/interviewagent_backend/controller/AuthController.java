package com.interviewagent.interviewagent_backend.controller;

import com.interviewagent.interviewagent_backend.DTO.*;
import com.interviewagent.interviewagent_backend.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> registerUser(@RequestBody SignUpRequest request){
        logger.info("Signup attempt for {}", request.getEmail());
        SignUpResponse response = authService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<OtpVerificationResponse> verifyOtp(@RequestBody OtpVerificationRequest request){
        logger.info("OTP verification attempt for {}", request.getEmail());
        OtpVerificationResponse response = authService.verifyOtp(request);

        if(!response.isOtpVerified()){
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request, HttpServletResponse response){
        logger.info("Login attempt for {}", request.getEmail());
        LoginResponse loginResponse = authService.login(request);

//        Cookie jwtCookie = new Cookie("jwt", loginResponse.getToken());
//        jwtCookie.setHttpOnly(true);
//        jwtCookie.setSecure(true);
//        jwtCookie.setPath("/");
//        jwtCookie.setMaxAge(7 * 24 * 60 * 60);
//
//        response.addCookie(jwtCookie);

        return ResponseEntity.ok("Login Succesful");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request, HttpServletResponse response){
        logger.info("Password reset attempt for {}", request.getEmail());
        SignUpResponse resetResponse = authService.resetPassword(request);
        if(!resetResponse.isOtpSent()){
            return ResponseEntity.badRequest().body(resetResponse);
        }
        return ResponseEntity.ok("Password Reset Successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request, HttpServletResponse response){
        logger.info("Forgot Password OTP request for {}", request.getEmail());
        SignUpResponse resetResponse = authService.sendOtpForForgotPassword(request);
        if(!resetResponse.isOtpSent()){
            return ResponseEntity.badRequest().body(resetResponse);
        }
        return ResponseEntity.ok("Password request accepted");
    }

    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")// only allow access to this method if the currently authenticated user has ADMIN role
    //EnableGlobalMethodSecurity should be enabled for thi
    public ResponseEntity<String> adminOnlyEndpoint(){
        return ResponseEntity.ok("Hello Admin");
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String>adminDashboard(){
        return ResponseEntity.ok("Welcom Admin");
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/profile")
    public ResponseEntity<String>userProfile(){
        return ResponseEntity.ok("Hello User");
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

}
