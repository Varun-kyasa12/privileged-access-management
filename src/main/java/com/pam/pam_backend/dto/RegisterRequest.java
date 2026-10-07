package com.pam.pam_backend.dto;
import jakarta.validation.constraints.*;
public record RegisterRequest(@NotBlank @Size(max=100) String name,@NotBlank @Email @Size(max=150) String email,@NotBlank @Size(min=10,max=72) String password){}
