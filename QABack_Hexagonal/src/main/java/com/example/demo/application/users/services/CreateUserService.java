package com.example.demo.application.users.services;


import com.example.demo.application.users.port.in.CreateUserUseCase;
import com.example.demo.application.users.port.out.PasswordHasherPort;
import com.example.demo.application.users.port.out.UserRepositoryPort;
import com.example.demo.domain.User;
import org.springframework.stereotype.Service;

@Service
public class CreateUserService implements CreateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;

    public CreateUserService(UserRepositoryPort userRepositoryPort, PasswordHasherPort passwordHasherPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
    }
    @Override
    public User createUser(User user) {
        String hashedPassword = passwordHasherPort.hash(user.getPassword());
        user.changePassword(hashedPassword);
        return userRepositoryPort.save(user);
    }
}
