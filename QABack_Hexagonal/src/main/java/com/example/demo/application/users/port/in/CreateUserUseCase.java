package com.example.demo.application.users.port.in;

import com.example.demo.domain.User;
import com.example.demo.domain.Vehicle;

public interface CreateUserUseCase {
    User createUser(User user);
}
