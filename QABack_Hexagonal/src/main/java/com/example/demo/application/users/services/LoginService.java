package com.example.demo.application.users.services;

import com.example.demo.application.users.port.in.LoginUseCase;
import com.example.demo.application.users.port.out.JwtPort;
import com.example.demo.application.users.port.out.PasswordHasherPort;
import com.example.demo.application.users.port.out.UserRepositoryPort;
import com.example.demo.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LoginService implements LoginUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final JwtPort jwtPort;
    private final PasswordHasherPort passwordHasherPort;

    public LoginService(UserRepositoryPort userRepositoryPort, JwtPort jwtPort, PasswordHasherPort passwordHasherPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.jwtPort = jwtPort;
        this.passwordHasherPort = passwordHasherPort;
    }

    @Override
    public String login(String email, String password) {
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordHasherPort.verify(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return jwtPort.generateToken(user);
    }
}
