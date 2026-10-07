package com.pam.pam_backend.controller;
import com.pam.pam_backend.dto.ApiResponse;
import com.pam.pam_backend.entity.User;
import com.pam.pam_backend.service.AuthorizationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
public class UserController {
 private final AuthorizationService auth;public UserController(AuthorizationService auth){this.auth=auth;}
 @GetMapping({"/users-test","/auth/me"}) public ApiResponse<?> current(@AuthenticationPrincipal User user){return ApiResponse.ok("Authenticated with SafeAccess",auth.profile(user));}
 @GetMapping("/applications") public ApiResponse<?> apps(@AuthenticationPrincipal User user){return ApiResponse.ok("Permitted applications",auth.applications(user.getId()));}
}