package com.example.demo.application.users.port.in;

import com.example.demo.domain.User;
import com.example.demo.domain.Vehicle;

import java.util.Optional;

public interface GetUserUseCase {
    Optional<User> getUser(Long id);
}
