package com.pam.pam_backend.controller;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import com.pam.pam_backend.dto.*;
import com.pam.pam_backend.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/auth")
public class AuthController {
 private final AuthService auth;private final MfaService mfa;private final RateLimitService limiter;private final AuditLogService audit;
 public AuthController(AuthService auth,MfaService mfa,RateLimitService limiter,AuditLogService audit){this.auth=auth;this.mfa=mfa;this.limiter=limiter;this.audit=audit;}
 @PostMapping("/register") public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,HttpServletRequest http){
  limiter.check("register:"+http.getRemoteAddr());var user=auth.register(request);audit.record(user.getId(),"REGISTER","AUTH","SUCCESS");
  return ResponseEntity.status(201).body(ApiResponse.ok("Registration successful",Map.of("id",user.getId(),"email",user.getEmail())));
 }
 @PostMapping("/login") public Map<String,Object> login(@Valid @RequestBody LoginRequest request,HttpServletRequest http){
  limiter.check("login-ip:"+http.getRemoteAddr());limiter.check("login-user:"+AuthService.email(request.email()));return auth.login(request);
 }
 @PostMapping("/send-otp") public Map<String,Object> send(@Valid @RequestBody OtpRequest request,HttpServletRequest http){
  limiter.check("resend:"+http.getRemoteAddr());return mfa.resend(request.email(),request.challengeToken());
 }
 @PostMapping("/verify-otp") public Map<String,Object> verify(@Valid @RequestBody VerifyOtpRequest request){
  return mfa.verify(request.email(),request.otp(),request.challengeToken());
 }
}
