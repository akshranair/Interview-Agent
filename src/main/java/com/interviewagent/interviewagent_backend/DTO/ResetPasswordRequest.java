package com.interviewagent.interviewagent_backend.DTO;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email;
    private String otp;
    private String token;
}
