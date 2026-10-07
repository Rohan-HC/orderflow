package com.rohan.orderflow.auth;

import com.rohan.orderflow.user.User;
import com.rohan.orderflow.user.UserRepository;
import com.rohan.orderflow.user.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {

        String normalizedEmail =
                request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        String passwordHash =
                passwordEncoder.encode(request.password());

        User user = new User(
                normalizedEmail,
                passwordHash,
                UserRole.CUSTOMER
        );

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }
}