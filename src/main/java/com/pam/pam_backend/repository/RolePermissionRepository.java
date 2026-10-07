package com.pam.pam_backend.repository;
import java.util.List;
import com.pam.pam_backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RolePermissionRepository extends JpaRepository<RolePermission,RolePermissionId>{
 List<RolePermission> findByIdRoleId(Long id);
 void deleteByIdRoleId(Long id);
}
