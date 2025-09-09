package com.example.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    /* TODO:
     * publicコンストラクタで実装してよいのか？
     * ChatGPTではprivateコンストラクタでnewする方法をすすめられた
    */
    
    private final String userId;
    
    private final String username;
    
    private final String password;
    
    private final Collection<? extends GrantedAuthority> authorities;

}
