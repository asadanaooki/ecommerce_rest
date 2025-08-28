package com.example.util;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.example.entity.User;

@Component
public class UserUtil {

    public static String buildFullName(User user) {
        return user.getLastName() + " " + user.getFirstName();
    }

    public static String buildFullAddress(User user) {
        return String.join("",
                user.getAddressPrefCity(),
                user.getAddressArea(),
                user.getAddressBlock(),
                Objects.toString(user.getAddressBuilding(), ""));
    }
}
