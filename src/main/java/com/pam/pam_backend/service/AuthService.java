package com.pam.pam_backend.service;
import java.time.LocalDateTime;
import java.util.*;
import com.pam.pam_backend.dto.*;
import com.pam.pam_backend.entity.*;
import com.pam.pam_backend.exception.ApiException;
import com.pam.pam_backend.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class AuthService {
 private final UserRepository users;private final RoleRepository roles;private final UserRoleRepository userRoles;
 private final PasswordEncoder encoder;private final MfaService mfa;private final AuditLogService audit;
 public AuthService(UserRepository users,RoleRepository roles,UserRoleRepository userRoles,PasswordEncoder encoder,MfaService mfa,AuditLogService audit){
 this.users=users;this.roles=roles;this.userRoles=userRoles;this.encoder=encoder;this.mfa=mfa;this.audit=audit;}
 public static String email(String email){return email.trim().toLowerCase(Locale.ROOT);}
 public static void password(String password){
  if(password==null || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72 || password.length()<10)
   throw new ApiException(400,"Password must be at least 10 characters and at most 72 UTF-8 bytes");
 }
 @Transactional public User register(RegisterRequest request){
  String email=email(request.email());password(request.password());
  if(users.existsByEmail(email))throw new ApiException(409,"Email already registered");
  Role employee=roles.findByName("EMPLOYEE").orElseThrow(()->new ApiException(503,"Default role is not configured"));
  User user=new User();user.setName(request.name().trim());user.setEmail(email);user.setPasswordHash(encoder.encode(request.password()));
  user.setStatus("ACTIVE");user.setCreatedAt(LocalDateTime.now());users.saveAndFlush(user);
  userRoles.save(new UserRole(new UserRoleId(user.getId(),employee.getId())));return user;
 }
 public Map<String,Object> login(LoginRequest request){
  User user=users.findByEmail(email(request.email())).orElse(null);
  if(user==null || !encoder.matches(request.password(),user.getPasswordHash()) || !"ACTIVE".equals(user.getStatus())){
   audit.record(user==null?null:user.getId(),"PASSWORD_LOGIN","AUTH","DENIED");
   throw new ApiException(401,"Invalid email or password");
  }
  audit.record(user.getId(),"PASSWORD_VERIFIED","AUTH","SUCCESS");return mfa.begin(user.getId());
 }
}
