package com.example.demo.adapter.user.inbound.mapper;

import com.example.demo.adapter.user.inbound.dto.user.UserRequest;
import com.example.demo.adapter.user.inbound.dto.user.UserResponse;
import com.example.demo.adapter.user.outbound.entities.UserEntity;
import com.example.demo.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    // DTO Request -> Dominio
    public User toDomain(UserRequest request) {
        return new User(
                request.fullName(),
                request.email(),
                request.password(),
                request.role(),
                request.status()
        );
    }

    // Entidad -> Dominio
    public User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getRole(),
                entity.getStatus()
        );
    }

    // Dominio -> Entidad
    public UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }

    // Dominio -> DTO Response
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }

}
