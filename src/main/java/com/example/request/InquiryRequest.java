package com.example.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

import lombok.Data;

@Data
public class InquiryRequest {
    // TODO:
    //  電話番号はハイフンなし前提
    //  現状半角数字のみ→ユーザーフレンドリーにする

    @NotBlank
    @Length(max = 50)
    private String lastName;
    
    @NotBlank
    @Length(max = 50)
    private String firstName;

    @Email
    @Length(max = 254)
    @NotBlank
    private String email;
    
    @NotBlank
    @Length(min = 11, max = 11)
    @Pattern(regexp = "^[0-9]+$")
    private String phoneNumber;
    
    @Length(min = 1, max = 4)
    @Pattern(regexp = "^[0-9]+$")
    private String orderNo;
    
    @NotBlank
    @Length(max = 1000)
    private String message;
}
