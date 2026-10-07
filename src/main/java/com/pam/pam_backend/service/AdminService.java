package com.pam.pam_backend.service;
import java.time.LocalDateTime;
import java.util.*;
import com.pam.pam_backend.dto.*;
import com.pam.pam_backend.entity.*;
import com.pam.pam_backend.repository.*;
import com.pam.pam_backend.exception.ApiException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class AdminService {
 private final UserRepository users;private final RoleRepository roles;private final UserRoleRepository userRoles;
 private final RolePermissionRepository rolePermissions;private final PermissionRepository permissions;private final PasswordEncoder encoder;private final AuthorizationService auth;
 public AdminService(UserRepository users,RoleRepository roles,UserRoleRepository userRoles,RolePermissionRepository rolePermissions,PermissionRepository permissions,PasswordEncoder encoder,AuthorizationService auth){
 this.users=users;this.roles=roles;this.userRoles=userRoles;this.rolePermissions=rolePermissions;this.permissions=permissions;this.encoder=encoder;this.auth=auth;}
 public Map<String,Object> view(User u){return Map.of("id",u.getId(),"name",u.getName(),"email",u.getEmail(),"status",u.getStatus(),"roles",auth.roles(u.getId()));}
 public List<Map<String,Object>> users(){return users.findAll().stream().map(this::view).toList();}
 public List<Map<String,Object>> roles(){return roles.findAll().stream().map(this::roleView).toList();}
 public List<Permission> permissions(){return permissions.findAll();}
 private Map<String,Object> roleView(Role r){
  List<Long> ids=rolePermissions.findByIdRoleId(r.getId()).stream().map(p->p.getId().getPermissionId()).toList();
  return Map.of("id",r.getId(),"name",r.getName(),"permissionIds",ids,"permissions",permissions.findAllById(ids));
 }
 private void checkRoles(Set<Long> ids){if(roles.findAllById(ids).size()!=ids.size())throw new ApiException(400,"Unknown role ID");}
 @Transactional public Map<String,Object> create(AdminUserRequest request){
  roles.lockAll();checkRoles(request.roleIds());AuthService.password(request.password());
  String email=AuthService.email(request.email());if(users.existsByEmail(email))throw new ApiException(409,"Email already registered");
  User user=new User();user.setCreatedAt(LocalDateTime.now());user.setName(request.name().trim());user.setEmail(email);
  user.setPasswordHash(encoder.encode(request.password()));user.setStatus(request.status()==null?"ACTIVE":request.status());users.saveAndFlush(user);
  request.roleIds().forEach(id->userRoles.save(new UserRole(new UserRoleId(user.getId(),id))));return view(user);
 }
 @Transactional public Map<String,Object> update(Long actor,Long id,AdminUserRequest request){
  roles.lockAll();checkRoles(request.roleIds());User user=users.findById(id).orElseThrow(()->new ApiException(404,"User not found"));
  String email=AuthService.email(request.email());
  if(!user.getEmail().equals(email)&&users.existsByEmail(email))throw new ApiException(409,"Email already registered");
  if(actor.equals(id)){
   Set<Long> permissionIds=new HashSet<>();
   request.roleIds().forEach(r->rolePermissions.findByIdRoleId(r).forEach(p->permissionIds.add(p.getId().getPermissionId())));
   Set<String> names=new HashSet<>();permissions.findAllById(permissionIds).forEach(p->names.add(p.getName()));
   if("INACTIVE".equals(request.status()) || !names.containsAll(Set.of("USER_VIEW","USER_MANAGE")))
    throw new ApiException(400,"You cannot remove your own administrative access");
  }
  user.setName(request.name().trim());user.setEmail(email);if(request.status()!=null)user.setStatus(request.status());
  if(request.password()!=null){AuthService.password(request.password());user.setPasswordHash(encoder.encode(request.password()));}
  userRoles.deleteByIdUserId(id);userRoles.flush();
  request.roleIds().forEach(r->userRoles.save(new UserRole(new UserRoleId(id,r))));users.saveAndFlush(user);return view(user);
 }
 @Transactional public void deactivate(Long actor,Long id){
  roles.lockAll();if(actor.equals(id))throw new ApiException(400,"You cannot deactivate your own account");
  User user=users.findById(id).orElseThrow(()->new ApiException(404,"User not found"));user.setStatus("INACTIVE");users.save(user);
 }
 @Transactional public Map<String,Object> saveRole(Long actor,Long id,RoleRequest request){
  roles.lockAll();
  if(permissions.findAllById(request.permissionIds()).size()!=request.permissionIds().size())throw new ApiException(400,"Unknown permission ID");
  Role role=id==null?new Role():roles.findById(id).orElseThrow(()->new ApiException(404,"Role not found"));
  if(id!=null && Set.of("ADMIN","EMPLOYEE","HR","MANAGER").contains(role.getName()) && !role.getName().equals(request.name()))
   throw new ApiException(400,"Built-in role names cannot be changed");
  if("ADMIN".equals(role.getName()))throw new ApiException(400,"The built-in ADMIN permission set is protected");
  if(roles.findByName(request.name()).filter(r->!Objects.equals(r.getId(),id)).isPresent())throw new ApiException(409,"Role name already exists");
  // Preserve at least one active administrator when editing an assigned custom role.
  if(id!=null && auth.roles(actor).stream().anyMatch(r->r.getId().equals(id)))throw new ApiException(400,"You cannot edit permissions of your own assigned role");
  role.setName(request.name());roles.saveAndFlush(role);
  rolePermissions.deleteByIdRoleId(role.getId());rolePermissions.flush();
  request.permissionIds().forEach(p->rolePermissions.save(new RolePermission(new RolePermissionId(role.getId(),p))));return roleView(role);
 }
}