package com.smartemi.dto;

import jakarta.validation.constraints.*;

public class RegisterRequest {
    @NotBlank(message = "Full name is required") @Size(max = 100)
    private String fullName;
    @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") @Size(max = 150)
    private String email;
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
             message = "Password must be 8-64 characters with at least one letter and one digit")
    private String password;

    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { this.password = v; }
}
