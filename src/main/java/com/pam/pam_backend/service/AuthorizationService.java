package com.pam.pam_backend.service;
import java.util.*;
import com.pam.pam_backend.entity.*;
import com.pam.pam_backend.exception.ApiException;
import com.pam.pam_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class AuthorizationService {
 private final UserRoleRepository userRoles;private final RolePermissionRepository rolePermissions;
 private final PermissionRepository permissions;private final RoleRepository roles;
 private final ApplicationRepository applications;private final AuditLogService audit;
 public AuthorizationService(UserRoleRepository userRoles,RolePermissionRepository rolePermissions,PermissionRepository permissions,
 RoleRepository roles,ApplicationRepository applications,AuditLogService audit){
 this.userRoles=userRoles;this.rolePermissions=rolePermissions;this.permissions=permissions;this.roles=roles;this.applications=applications;this.audit=audit;}
 @Transactional(readOnly=true) public List<Role> roles(Long id){
  return roles.findAllById(userRoles.findByIdUserId(id).stream().map(r->r.getId().getRoleId()).toList()).stream().sorted(Comparator.comparing(Role::getName)).toList();
 }
 @Transactional(readOnly=true) public Set<String> permissions(Long id){
  Set<Long> ids=new HashSet<>();
  userRoles.findByIdUserId(id).forEach(r->rolePermissions.findByIdRoleId(r.getId().getRoleId()).forEach(p->ids.add(p.getId().getPermissionId())));
  Set<String> result=new TreeSet<>();permissions.findAllById(ids).forEach(p->result.add(p.getName()));return result;
 }
 public boolean hasPermission(Long id,String permission){return permissions(id).contains(permission);}
 public void require(User user,String permission,String resource){
  boolean allowed=hasPermission(user.getId(),permission);
  audit.record(user.getId(),"AUTHORIZE_"+permission,resource,allowed?"SUCCESS":"DENIED");
  if(!allowed)throw new ApiException(403,"Access denied: "+permission+" permission required");
 }
 public void requireAdmin(User user,boolean read,String resource){require(user,"USER_MANAGE",resource);if(read)require(user,"USER_VIEW",resource);}
 public List<Map<String,Object>> applications(Long id){
  Set<String> allowed=permissions(id);List<Map<String,Object>> result=new ArrayList<>();
  for(Application app:applications.findAll()){
   List<String> granted=applications.permissionNames(app.getId()).stream().filter(allowed::contains).toList();
   if(granted.contains(app.getName()+"_VIEW"))result.add(Map.of("id",app.getId(),"name",app.getName(),"permissions",granted));
  }return result;
 }
 public Map<String,Object> profile(User u){return Map.of("id",u.getId(),"name",u.getName(),"email",u.getEmail(),"status",u.getStatus(),
 "roles",roles(u.getId()),"permissions",permissions(u.getId()),"applications",applications(u.getId()));}
}