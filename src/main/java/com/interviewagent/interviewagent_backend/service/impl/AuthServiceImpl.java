package com.interviewagent.interviewagent_backend.service.impl;

import com.interviewagent.interviewagent_backend.DTO.*;
import com.interviewagent.interviewagent_backend.entity.User;
import com.interviewagent.interviewagent_backend.enums.Role;
import com.interviewagent.interviewagent_backend.exception.EmailAlreadyExistsException;
import com.interviewagent.interviewagent_backend.repository.UserRepository;
import com.interviewagent.interviewagent_backend.service.JwtService;
import com.interviewagent.interviewagent_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public SignUpResponse registerUser(SignUpRequest request){
        Optional<User> account = userRepository.findByEmail(request.getEmail());
        if(account.isPresent()){
            throw new EmailAlreadyExistsException("Email Already exists");
        }

        int otp = (int)(Math.random() * 900000) + 100000;
        User user = new User();
        user.setEmail(request.getEmail());
        user.setSignUpOTP(otp);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        user.setVerified(false);
        user.setRole(Role.USER);
        userRepository.save(user);
        System.out.println(otp);
        return new SignUpResponse("OTP sent Succesfully", true);
    }

    @Override
    public OtpVerificationResponse verifyOtp(OtpVerificationRequest request){
        Optional<User> accountOptional = userRepository.findByEmail(request.getEmail());
        if(!accountOptional.isPresent()){
            return  new OtpVerificationResponse("No user registered with this mail", false);
        }

        User account = accountOptional.get();
        if(account.getOtpExpiry().isBefore(LocalDateTime.now())){
            return new OtpVerificationResponse("OTP expired, please request a new one", false);
        }

        if(request.isForPasswordReset()) {
            if (account.getResetPasswordOTP() != (request.getOtp())) {
                return new OtpVerificationResponse("Otp doesn't match", false);
            }
            account.setSignUpOTP(0);
            account.setOtpExpiry(null);
            return new OtpVerificationResponse("Otp Verified, you may now reset password", true);
        }

        if (account.getSignUpOTP() != (request.getOtp())) {
            return new OtpVerificationResponse("Otp doesn't match", false);
        }
        account.setResetPasswordOTP(0);
        account.setOtpExpiry(null);
        if(account.isVerified()){
            return new OtpVerificationResponse("Account already verified", true);
        }

        account.setVerified(true);
        userRepository.saveAndFlush(account);
        return new OtpVerificationResponse("User Verified Successfully", true);
    }

    @Override
    public LoginResponse login(LoginRequest request){
        Optional<User>account = userRepository.findByEmail(request.getEmail());

        if(account.isEmpty()){
            throw  new RuntimeException("User not found");
        }

        User user = account.get();

        if(!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            throw new RuntimeException("Invalid Details");
        }

        if(!user.isVerified()){
            throw new RuntimeException("Invalid Details");
        }

        String token = jwtService.generateToken(request.getEmail(), user.getRole().name(), Map.of());
        return new LoginResponse(token, "Login Successful");
    }

    @Override
    public SignUpResponse sendOtpForForgotPassword(ForgotPasswordRequest request) {
        Optional<User>account = userRepository.findByEmail(request.getEmail());
        if(account.isEmpty()){

            return new SignUpResponse("Account not found", false);
        }
        int otp = (int)(Math.random() * 900000) + 100000;
        System.out.println(otp);
        User user = account.get();
        user.setResetPasswordOTP(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.saveAndFlush(user);
        return new SignUpResponse("OTP send for password request", true);
    }

    @Override
    public SignUpResponse resetPassword(ResetPasswordRequest request) {
        Optional<User>account = userRepository.findByEmail(request.getEmail());
        if(account.isEmpty()){
            return new SignUpResponse("Account not found", false);
        }
        User user = account.get();
        if(!request.getNewPassword().equals(request.getConfirmPassword())){
            return new SignUpResponse("Passwords don't match", false);
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.saveAndFlush(user);
        return new SignUpResponse("Password reset successfully", true);
    }


}
