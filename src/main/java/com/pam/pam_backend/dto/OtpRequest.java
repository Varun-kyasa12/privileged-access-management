package com.pam.pam_backend.dto;
import jakarta.validation.constraints.*;
public record OtpRequest(@NotBlank @Email @Size(max=150) String email,@NotBlank @Size(max=2048) String challengeToken){}
