package com.pam.pam_backend.repository;
import java.util.List;
import com.pam.pam_backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRoleRepository extends JpaRepository<UserRole,UserRoleId>{
 List<UserRole> findByIdUserId(Long id);
 void deleteByIdUserId(Long id);
}
