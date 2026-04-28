package com.example.demo.application.users.services;

import com.example.demo.application.users.port.in.UpdateUserUseCase;
import com.example.demo.application.users.port.out.PasswordHasherPort;
import com.example.demo.application.users.port.out.UserRepositoryPort;

import com.example.demo.domain.User;
import org.springframework.stereotype.Service;

@Service
public class UpdateUserService implements UpdateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;

    public UpdateUserService(UserRepositoryPort userRepositoryPort, PasswordHasherPort passwordHasherPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
    }

    @Override
    public User updateUser(Long id, String fullName, String email, String password, String role, Boolean status) {
        User existing = userRepositoryPort.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User with id " + id + " not found"
                        )
                );

        String encryptedPassword = (password != null && !password.isBlank())
                ? passwordHasherPort.hash(password)
                : existing.getPassword();
        existing.updateData(fullName,email,encryptedPassword,role,status);
        return userRepositoryPort.save(existing);
    }
}
