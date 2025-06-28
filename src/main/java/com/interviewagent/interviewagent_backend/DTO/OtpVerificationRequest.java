package com.interviewagent.interviewagent_backend.DTO;

import lombok.Data;

@Data
public class OtpVerificationRequest {
    String email;
    String otp;
}
