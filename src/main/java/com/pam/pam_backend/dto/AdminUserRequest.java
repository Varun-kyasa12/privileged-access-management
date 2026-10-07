package com.pam.pam_backend.dto;
import jakarta.validation.constraints.*;
public record AdminUserRequest(@NotBlank @Size(max=100) String name,@NotBlank @Email @Size(max=150) String email,@Size(min=10,max=72) String password,@Pattern(regexp="ACTIVE|INACTIVE") String status,@NotNull @Size(min=1,max=20) java.util.Set<@Positive Long> roleIds){}
