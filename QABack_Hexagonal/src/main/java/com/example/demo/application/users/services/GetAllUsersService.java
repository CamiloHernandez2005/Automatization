package com.example.demo.application.users.services;


import com.example.demo.application.users.port.in.GetAllUsersUseCase;
import com.example.demo.application.users.port.out.UserRepositoryPort;
import com.example.demo.domain.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllUsersService implements GetAllUsersUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public GetAllUsersService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepositoryPort.findAll();
    }
}
