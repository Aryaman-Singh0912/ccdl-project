package com.ccd.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecretKey key;

    public AuthController(UserRepository users, @Value("${jwt.secret}") String secret) {
        this.users = users;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public record Credentials(String email, String password) {}
    public record AuthResponse(String token, String email) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody Credentials c) {
        String email = clean(c.email());
        if (email == null || !email.contains("@") || c.password() == null || c.password().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A valid email and a password of at least 6 characters are required");
        }
        if (users.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        AppUser u = new AppUser();
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(c.password()));
        u = users.save(u);
        log.info("Registered new user id={}", u.getId());
        return new AuthResponse(token(u), u.getEmail());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody Credentials c) {
        String email = clean(c.email());
        AppUser u = (email == null ? java.util.Optional.<AppUser>empty() : users.findByEmail(email))
                .filter(x -> c.password() != null && encoder.matches(c.password(), x.getPasswordHash()))
                .orElseThrow(() -> {
                    log.warn("Failed login attempt");
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password");
                });
        log.info("User id={} logged in", u.getId());
        return new AuthResponse(token(u), u.getEmail());
    }

    private String token(AppUser u) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(u.getId()))
                .issuedAt(new Date(now))
                .expiration(new Date(now + 8L * 3600 * 1000))
                .signWith(key)
                .compact();
    }

    private String clean(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
