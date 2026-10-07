package com.pam.pam_backend.repository;
import java.util.*;
import com.pam.pam_backend.entity.Role;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
public interface RoleRepository extends JpaRepository<Role,Long>{
 Optional<Role> findByName(String name);
 boolean existsByName(String name);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from Role r order by r.id") List<Role> lockAll();
}
