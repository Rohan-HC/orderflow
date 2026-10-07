package com.rohan.orderflow.auth;

import com.rohan.orderflow.user.User;
import com.rohan.orderflow.user.UserRole;

public record UserResponse(
        Long id,
        String email,
        UserRole role
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
    }
}