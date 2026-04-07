package com.example.demo.application.users.services;

import com.example.demo.application.users.port.in.DeleteUserUseCase;
import com.example.demo.application.users.port.out.UserRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class DeleteUserService implements DeleteUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public DeleteUserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public void deleteUser(Long id) {
        userRepositoryPort.deleteById(id);

    }
}
