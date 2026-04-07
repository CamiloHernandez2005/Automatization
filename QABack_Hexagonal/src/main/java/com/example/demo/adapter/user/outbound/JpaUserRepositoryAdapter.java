package com.example.demo.adapter.user.outbound;

import com.example.demo.adapter.user.inbound.mapper.UserMapper;
import com.example.demo.adapter.user.outbound.entities.UserEntity;
import com.example.demo.adapter.user.outbound.repository.UserRepository;
import com.example.demo.application.users.port.out.UserRepositoryPort;
import com.example.demo.domain.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public JpaUserRepositoryAdapter(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity saved = userRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
