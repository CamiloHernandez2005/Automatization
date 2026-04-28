package com.example.demo.domain;

import java.util.Objects;

public class User {
    private Long id;
    private String fullName;
    private String email;
    private String password;
    private String role;
    private Boolean status;

    public User(Long id, String fullName, String email, String password, String role, Boolean status) {
        this.id = id;
        this.fullName= Objects.requireNonNull(fullName, "full name cannot be null");
        this.email= Objects.requireNonNull(email, "model cannot be null");
        this.password= Objects.requireNonNull(password, "password cannot be null");
        this.role= Objects.requireNonNull(role, "licensePlate cannot be null");
        this.status= Objects.requireNonNull(status, "status cannot be null");
    }

    public User(String fullName, String email, String password, String role, Boolean status)  {
        this(null, fullName, email, password, role, status);
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public  Boolean getStatus() {return status;}

    public void changePassword(String hashedPassword) {
        this.password = Objects.requireNonNull(hashedPassword, "password cannot be null");
    }

    public void changeStatus(Boolean status) {
        this.status = Objects.requireNonNull(status, "status cannot be null");
    }

    public void updateData(String fullName,
                           String email,
                           String password,
                           String role,
                           Boolean status) {

        this.fullName = Objects.requireNonNull(fullName, "full name cannot be null");
        this.email = Objects.requireNonNull(email, "email cannot be null");
        this.password = Objects.requireNonNull(password, "password cannot be null");
        this.role = Objects.requireNonNull(role, "role cannot be null");
        this.status = Objects.requireNonNull(status, "status cannot be null");
    }

}
