package com.interviewagent.interviewagent_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtpVerificationResponse {
    private String message;
    private boolean otpVerified;
}
