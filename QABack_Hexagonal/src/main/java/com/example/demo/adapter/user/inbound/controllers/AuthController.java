package com.example.demo.adapter.user.inbound.controllers;

import com.example.demo.adapter.user.inbound.dto.login.LoginRequest;
import com.example.demo.application.users.port.in.LoginUseCase;
import com.example.demo.application.users.port.in.ValidateTokenUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final ValidateTokenUseCase validateTokenUseCase;

    public AuthController(LoginUseCase loginUseCase,
                          ValidateTokenUseCase validateTokenUseCase) {
        this.loginUseCase = loginUseCase;
        this.validateTokenUseCase = validateTokenUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        String token = loginUseCase.login(request.email(), request.password());

        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Login correcto"));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(
            @CookieValue(value = "jwt", required = false) String token) {

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false));
        }

        boolean valid = validateTokenUseCase.validateToken(token);
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {

        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Logout correcto"));
    }
}
