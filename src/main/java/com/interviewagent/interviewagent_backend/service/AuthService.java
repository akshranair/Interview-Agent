package com.interviewagent.interviewagent_backend.service;

import com.interviewagent.interviewagent_backend.DTO.*;


public interface AuthService {
    SignUpResponse registerUser(SignUpRequest request);
    OtpVerificationResponse verifyOtp(OtpVerificationRequest request);
    LoginResponse login(LoginRequest request);
    SignUpResponse sendOtpForForgotPassword(ForgotPasswordRequest request);
    SignUpResponse resetPassword(ResetPasswordRequest request);
}
