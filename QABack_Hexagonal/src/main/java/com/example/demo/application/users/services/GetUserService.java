package com.example.demo.application.users.services;


import com.example.demo.application.users.port.in.GetUserUseCase;
import com.example.demo.application.users.port.out.UserRepositoryPort;
import com.example.demo.domain.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetUserService implements GetUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public GetUserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public Optional<User> getUser(Long id) {
        return userRepositoryPort.findById(id);
    }
}
