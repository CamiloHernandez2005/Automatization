package com.example.demo.application.users.services;

import com.example.demo.application.users.port.in.UpdateUserStatusUseCase;
import com.example.demo.application.users.port.out.UserRepositoryPort;
import com.example.demo.domain.User;
import org.springframework.stereotype.Service;

@Service
public class UpdateUserStatusService implements UpdateUserStatusUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public UpdateUserStatusService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User updateStatus(Long id, Boolean status) {
        User existing = userRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with id " + id + " not found"));
        existing.changeStatus(status);
        return userRepositoryPort.save(existing);
    }
}
