package com.interviewagent.interviewagent_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse
{
    private String message;
    private String token;
}
