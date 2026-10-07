package com.pam.pam_backend.repository;
import java.util.Optional;
import com.pam.pam_backend.entity.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
public interface UserRepository extends JpaRepository<User,Long>{
 boolean existsByEmail(String email);
 Optional<User> findByEmail(String email);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select u from User u where u.id=:id")
 Optional<User> findLockedById(@Param("id") Long id);
}
