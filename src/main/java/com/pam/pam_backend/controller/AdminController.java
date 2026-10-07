package com.pam.pam_backend.controller;
import jakarta.validation.Valid;
import com.pam.pam_backend.dto.*;
import com.pam.pam_backend.entity.User;
import com.pam.pam_backend.exception.ApiException;
import com.pam.pam_backend.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
public class AdminController {
 private final AdminService service;private final AuthorizationService auth;private final AuditLogService audit;
 public AdminController(AdminService service,AuthorizationService auth,AuditLogService audit){this.service=service;this.auth=auth;this.audit=audit;}
 private void id(Long id){if(id<=0)throw new ApiException(400,"ID must be positive");}
 @GetMapping("/admin/users") public ApiResponse<?> users(@AuthenticationPrincipal User user){auth.requireAdmin(user,true,"ADMIN_USERS");return ApiResponse.ok("Users",service.users());}
 @PostMapping("/admin/users") public ResponseEntity<?> create(@AuthenticationPrincipal User user,@Valid @RequestBody AdminUserRequest request){
 auth.requireAdmin(user,false,"ADMIN_USERS");var result=service.create(request);audit.record(user.getId(),"USER_CREATED","USER:"+result.get("id"),"SUCCESS");return ResponseEntity.status(201).body(ApiResponse.ok("User created",result));}
 @PutMapping("/admin/users/{id}") public ApiResponse<?> update(@AuthenticationPrincipal User user,@PathVariable Long id,@Valid @RequestBody AdminUserRequest request){
 id(id);auth.requireAdmin(user,false,"ADMIN_USERS");var result=service.update(user.getId(),id,request);audit.record(user.getId(),"USER_UPDATED","USER:"+id,"SUCCESS");return ApiResponse.ok("User updated",result);}
 @DeleteMapping("/admin/users/{id}") public ApiResponse<?> remove(@AuthenticationPrincipal User user,@PathVariable Long id){
 id(id);auth.requireAdmin(user,false,"ADMIN_USERS");service.deactivate(user.getId(),id);audit.record(user.getId(),"USER_DEACTIVATED","USER:"+id,"SUCCESS");return ApiResponse.ok("User deactivated; history retained",null);}
 @GetMapping({"/admin/roles","/roles"}) public ApiResponse<?> roles(@AuthenticationPrincipal User user){auth.requireAdmin(user,true,"ADMIN_ROLES");return ApiResponse.ok("Roles",service.roles());}
 @GetMapping("/permissions") public ApiResponse<?> permissions(@AuthenticationPrincipal User user){auth.requireAdmin(user,true,"PERMISSIONS");return ApiResponse.ok("Permission catalog",service.permissions());}
 @PostMapping("/admin/roles") public ResponseEntity<?> createRole(@AuthenticationPrincipal User user,@Valid @RequestBody RoleRequest request){
 auth.requireAdmin(user,false,"ADMIN_ROLES");var result=service.saveRole(user.getId(),null,request);audit.record(user.getId(),"ROLE_CREATED","ROLE:"+result.get("id"),"SUCCESS");return ResponseEntity.status(201).body(ApiResponse.ok("Role created",result));}
 @PutMapping("/admin/roles/{id}") public ApiResponse<?> updateRole(@AuthenticationPrincipal User user,@PathVariable Long id,@Valid @RequestBody RoleRequest request){
 id(id);auth.requireAdmin(user,false,"ADMIN_ROLES");var result=service.saveRole(user.getId(),id,request);audit.record(user.getId(),"ROLE_UPDATED","ROLE:"+id,"SUCCESS");return ApiResponse.ok("Role updated",result);}
}