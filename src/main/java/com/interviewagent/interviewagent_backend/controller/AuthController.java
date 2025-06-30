package com.interviewagent.interviewagent_backend.controller;

import com.interviewagent.interviewagent_backend.DTO.*;
import com.interviewagent.interviewagent_backend.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> registerUser(@RequestBody SignUpRequest request){
        SignUpResponse response = authService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<OtpVerificationResponse> verifyOtp(@RequestBody OtpVerificationRequest request){
        OtpVerificationResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request, HttpServletResponse response){
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
        SignUpResponse resetResponse = authService.resetPassword(request);

        return ResponseEntity.ok("Password Reset Succesfull");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request, HttpServletResponse response){
        SignUpResponse resetResponse = authService.sendOtpForForgotPassword(request);

        return ResponseEntity.ok("Password request accepted");
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

}
