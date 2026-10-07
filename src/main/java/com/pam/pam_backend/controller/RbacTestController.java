package com.pam.pam_backend.controller;
import com.pam.pam_backend.dto.ApiResponse;
import com.pam.pam_backend.entity.User;
import com.pam.pam_backend.service.AuthorizationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
public class RbacTestController {
 private final AuthorizationService auth;public RbacTestController(AuthorizationService auth){this.auth=auth;}
 @GetMapping("/rbac-test") public ApiResponse<?> roles(@AuthenticationPrincipal User user){return ApiResponse.ok("Your roles",auth.roles(user.getId()));}
 @GetMapping("/permissions-test") public ApiResponse<?> permissions(@AuthenticationPrincipal User user){return ApiResponse.ok("Your effective permissions",auth.permissions(user.getId()));}
}