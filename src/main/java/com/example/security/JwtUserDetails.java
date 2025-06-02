package com.example.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtUserDetails implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public String getPassword() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public String getUsername() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

}
