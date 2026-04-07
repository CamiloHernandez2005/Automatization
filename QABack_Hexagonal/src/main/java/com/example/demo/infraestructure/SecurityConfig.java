package com.example.demo.infraestructure;


import com.example.demo.application.users.port.in.ValidateTokenUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ValidateTokenUseCase validateTokenUseCase;

    public SecurityConfig(ValidateTokenUseCase validateTokenUseCase) {
        this.validateTokenUseCase = validateTokenUseCase;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // CORS en Security
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowCredentials(true);
                    config.addAllowedOrigin("http://localhost:5173");
                    config.addAllowedHeader("*");
                    config.addAllowedMethod("*");
                    return config;
                }))

                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/logout").permitAll()
                        .requestMatchers("/api/auth/verify").permitAll()
                        .requestMatchers("/api/users").permitAll()
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        new JwtFilter(validateTokenUseCase),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}

