package com.pam.pam_backend.controller;
import java.util.Map;
import com.pam.pam_backend.dto.ApiResponse;
import com.pam.pam_backend.entity.User;
import com.pam.pam_backend.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
public class AuditController {
 private final AuthorizationService auth;private final AuditLogService audit;
 public AuditController(AuthorizationService auth,AuditLogService audit){this.auth=auth;this.audit=audit;}
 @GetMapping({"/audit","/admin/audit"}) public ApiResponse<?> list(@AuthenticationPrincipal User user,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="25")int size){
  auth.require(user,"AUDIT_VIEW","AUDIT");var records=audit.list(page,size);
  return ApiResponse.ok("Audit history",Map.of("content",records.getContent(),"number",records.getNumber(),"totalPages",records.getTotalPages(),"totalElements",records.getTotalElements()));
 }
}