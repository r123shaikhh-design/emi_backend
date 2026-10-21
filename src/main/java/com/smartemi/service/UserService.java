package com.smartemi.service;

import com.smartemi.dto.RegisterRequest;
import com.smartemi.entity.User;
import com.smartemi.exception.DuplicateEmailException;
import com.smartemi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users; this.encoder = encoder;
    }

    /** Registers a user; the password is stored only as a BCrypt hash. */
    @Transactional
    public User register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new DuplicateEmailException();
        return users.save(new User(req.getFullName().trim(), email, encoder.encode(req.getPassword())));
    }
}
