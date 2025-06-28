package com.interviewagent.interviewagent_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignUpResponse {
    private String message;
    private boolean otpSent;
}
