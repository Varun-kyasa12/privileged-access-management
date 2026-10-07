package com.pam.pam_backend.controller;
import com.pam.pam_backend.dto.ApiResponse;
import com.pam.pam_backend.entity.User;
import com.pam.pam_backend.service.DemoApplicationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
public class DemoApplicationController {
 private final DemoApplicationService service;public DemoApplicationController(DemoApplicationService service){this.service=service;}
 @GetMapping("/attendance") public ApiResponse<?> attendance(@AuthenticationPrincipal User user){return ApiResponse.ok("Attendance access granted",service.access(user,"ATTENDANCE",false));}
 @PostMapping("/attendance") public ApiResponse<?> manageAttendance(@AuthenticationPrincipal User user){return ApiResponse.ok("Attendance management authorized",service.access(user,"ATTENDANCE",true));}
 @GetMapping("/inventory") public ApiResponse<?> inventory(@AuthenticationPrincipal User user){return ApiResponse.ok("Inventory access granted",service.access(user,"INVENTORY",false));}
 @PostMapping("/inventory") public ApiResponse<?> manageInventory(@AuthenticationPrincipal User user){return ApiResponse.ok("Inventory management authorized",service.access(user,"INVENTORY",true));}
}