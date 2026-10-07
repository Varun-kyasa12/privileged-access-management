package com.pam.pam_backend.dto;
import jakarta.validation.constraints.*;
public record VerifyOtpRequest(@NotBlank @Email @Size(max=150) String email,@NotBlank @Pattern(regexp="[0-9]{6}") String otp,@NotBlank @Size(max=2048) String challengeToken){}
