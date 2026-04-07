package com.example.demo.application.users.port.out;

public interface PasswordHasherPort {
    String hash(String rawPassword);
    boolean verify(String rawPassword, String hashedPassword);
}
