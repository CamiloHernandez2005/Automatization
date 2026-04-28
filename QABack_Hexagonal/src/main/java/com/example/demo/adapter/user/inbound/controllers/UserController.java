package com.example.demo.adapter.user.inbound.controllers;


import com.example.demo.adapter.user.inbound.dto.user.StatusRequest;
import com.example.demo.adapter.user.inbound.dto.user.UserRequest;
import com.example.demo.adapter.user.inbound.dto.user.UserResponse;
import com.example.demo.adapter.user.inbound.mapper.UserMapper;
import com.example.demo.application.users.port.in.*;
import com.example.demo.application.users.port.in.UpdateUserStatusUseCase;
import com.example.demo.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetAllUsersUseCase getAllUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final UpdateUserStatusUseCase updateUserStatusUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final UserMapper mapper;


    public UserController(CreateUserUseCase createUserUseCase, GetAllUsersUseCase getAllUsersUseCase, UpdateUserUseCase updateUserUseCase, UpdateUserStatusUseCase updateUserStatusUseCase, DeleteUserUseCase deleteUserUseCase, GetUserUseCase getUserUseCase, UserMapper mapper) {
        this.createUserUseCase = createUserUseCase;
        this.getAllUsersUseCase = getAllUsersUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.updateUserStatusUseCase = updateUserStatusUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.getUserUseCase = getUserUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserRequest request){
        User user = mapper.toDomain(request);
        User created = createUserUseCase.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        List<UserResponse> component = getAllUsersUseCase.getAllUsers()
                .stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(component);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id){
        return getUserUseCase.getUser(id)
                .map(user -> ResponseEntity.ok(mapper.toResponse(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody UserRequest request){
        User updated = updateUserUseCase.updateUser(
                id,
                request.fullName(),
                request.email(),
                request.password(),
                request.role(),
                request.status()
        );
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        User updated = updateUserStatusUseCase.updateStatus(id, request.status());
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        deleteUserUseCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


}
