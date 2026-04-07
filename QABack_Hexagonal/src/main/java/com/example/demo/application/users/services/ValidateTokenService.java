package com.example.demo.application.users.services;

import com.example.demo.application.users.port.in.ValidateTokenUseCase;
import com.example.demo.application.users.port.out.JwtPort;
import org.springframework.stereotype.Service;

@Service
public class ValidateTokenService implements ValidateTokenUseCase {

    private final JwtPort jwtPort;

    public ValidateTokenService(JwtPort jwtPort) {
        this.jwtPort = jwtPort;
    }

    @Override
    public boolean validateToken(String token) {
        return jwtPort.validateToken(token);
    }
}
