package com.pam.pam_backend.controller;
import com.pam.pam_backend.dto.ApiResponse;
import com.pam.pam_backend.entity.User;
import com.pam.pam_backend.service.DemoApplicationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
public class HrController {
 private final DemoApplicationService service;public HrController(DemoApplicationService service){this.service=service;}
 @GetMapping("/hr") public ApiResponse<?> view(@AuthenticationPrincipal User user){return ApiResponse.ok("HR access granted",service.access(user,"HR",false));}
 @PostMapping("/hr") public ApiResponse<?> manage(@AuthenticationPrincipal User user){return ApiResponse.ok("HR management authorized",service.access(user,"HR",true));}
}