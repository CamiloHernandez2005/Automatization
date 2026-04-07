package com.example.demo.application.users.port.out;

import com.example.demo.domain.User;
import com.example.demo.domain.Vehicle;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    void deleteById(Long id);
}
