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
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
    @Transactional(readOnly = true)
public TokenResponse login(LoginRequest request) {

    String normalizedEmail =
            request.email().trim().toLowerCase();

    User user = userRepository
            .findByEmail(normalizedEmail)
            .orElseThrow(InvalidCredentialsException::new);

    boolean passwordMatches =
            passwordEncoder.matches(
                    request.password(),
                    user.getPasswordHash()
            );

    if (!passwordMatches) {
        throw new InvalidCredentialsException();
    }

    String token = jwtService.generateToken(user);

    return new TokenResponse(
            token,
            "Bearer",
            3600
    );
}
}
