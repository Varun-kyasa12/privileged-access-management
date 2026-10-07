package com.pam.pam_backend.controller;
import java.util.Map;
import com.pam.pam_backend.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;
@RestController public class TestController {
 @GetMapping("/hello") public ApiResponse<?> hello(){return ApiResponse.ok("SafeAccess - PAM Backend is running",Map.of("application","SafeAccess","service","PAM Backend"));}
}