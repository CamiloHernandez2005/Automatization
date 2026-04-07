package com.example.demo.application.users.port.in;

import com.example.demo.domain.User;

public interface UpdateUserUseCase {
    User updateUser(Long id, String fullName, String email, String password, String role, Boolean status);
}
