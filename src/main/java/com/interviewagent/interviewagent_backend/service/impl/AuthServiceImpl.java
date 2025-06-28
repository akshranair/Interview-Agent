package com.interviewagent.interviewagent_backend.service.impl;

import com.interviewagent.interviewagent_backend.DTO.SignUpRequest;
import com.interviewagent.interviewagent_backend.DTO.SignUpResponse;
import com.interviewagent.interviewagent_backend.entity.User;
import com.interviewagent.interviewagent_backend.exception.EmailAlreadyExistsException;
import com.interviewagent.interviewagent_backend.repository.UserRepository;
import com.interviewagent.interviewagent_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public SignUpResponse registerUser(SignUpRequest request){
        Optional<User> account = userRepository.findByEmail(request.getEmail());
        if(account.isPresent()){
            throw new EmailAlreadyExistsException("Email Already exists");
        }

        int otp = (int)(Math.random() * 900000) + 100000;
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.getEmail());
        user.setOtpCode(otp);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        user.setVerified(false);
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        return new SignUpResponse("OTP sent Succesfully", true);
    }
}
