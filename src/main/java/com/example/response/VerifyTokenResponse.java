package com.example.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VerifyTokenResponse {

    private final String token;

    private final String email;
}
