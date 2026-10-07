package com.pam.pam_backend.service;
import java.util.Map;
import com.pam.pam_backend.entity.User;
import org.springframework.stereotype.Service;
@Service
public class DemoApplicationService {
 private final AuthorizationService auth;private final AuditLogService audit;
 public DemoApplicationService(AuthorizationService auth,AuditLogService audit){this.auth=auth;this.audit=audit;}
 public Map<String,Object> access(User user,String application,boolean manage){
  String permission=application+(manage?"_MANAGE":"_VIEW");auth.require(user,permission,application);
  audit.record(user.getId(),manage?"DEMO_MANAGE":"APPLICATION_VIEW",application,"SUCCESS");
  return Map.of("application",application,"requiredPermission",permission,"canManage",auth.hasPermission(user.getId(),application+"_MANAGE"),
  "message",manage?"Management permission verified; demo action recorded in audit log":"Welcome to the "+application+" demo application");
 }
}