package com.pam.pam_backend.dto;
import jakarta.validation.constraints.*;
public record LoginRequest(@NotBlank @Email @Size(max=150) String email,@NotBlank @Size(max=72) String password){}
