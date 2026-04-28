package com.example.demo.application.users.port.in;

import com.example.demo.domain.User;

public interface UpdateUserStatusUseCase {
    User updateStatus(Long id, Boolean status);
}
