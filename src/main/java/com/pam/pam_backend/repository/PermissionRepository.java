package com.pam.pam_backend.repository;

import com.pam.pam_backend.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository
        extends JpaRepository<Permission, Long> {
}