package com.example.demo.application.users.port.out;

import com.example.demo.domain.User;

public interface JwtPort {
    String generateToken(User user);
    boolean validateToken(String token);
}
