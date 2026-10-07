package com.pam.pam_backend.dto;
import jakarta.validation.constraints.*;
public record RoleRequest(@NotBlank @Pattern(regexp="[A-Z][A-Z0-9_]{1,49}") String name,@NotNull @Size(max=100) java.util.Set<@Positive Long> permissionIds){}
