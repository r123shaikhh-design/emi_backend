package com.smartemi.controller;

import com.smartemi.dto.LoginRequest;
import com.smartemi.dto.RegisterRequest;
import com.smartemi.entity.User;
import com.smartemi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

/** JSON auth endpoints (session cookie based) for API clients such as Postman. */
@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {
    private final UserService userService;
    private final AuthenticationManager authManager;

    public ApiAuthController(UserService userService, AuthenticationManager authManager) {
        this.userService = userService; this.authManager = authManager;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        User u = userService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", u.getId(), "fullName", u.getFullName(), "email", u.getEmail()));
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest req,
                                     HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email().trim().toLowerCase(), req.password()));
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
        new HttpSessionSecurityContextRepository().saveContext(ctx, request, response);
        return Map.of("email", auth.getName(), "message", "Logged in");
    }

    /** Lets the React app restore the logged-in user after a page refresh. */
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(Map.of("email", principal.getName()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
}
