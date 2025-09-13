package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegistrationVerificationDto {

    private final String rawToken;

    private final String email;
}
