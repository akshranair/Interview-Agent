package com.interviewagent.interviewagent_backend.service;

import com.interviewagent.interviewagent_backend.DTO.SignUpRequest;
import com.interviewagent.interviewagent_backend.DTO.SignUpResponse;

public interface AuthService {
    SignUpResponse registerUser(SignUpRequest request);
}
