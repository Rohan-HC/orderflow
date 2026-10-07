package com.rohan.orderflow.auth;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}