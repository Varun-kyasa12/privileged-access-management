package com.pam.pam_backend.repository;
import java.util.*;
import com.pam.pam_backend.entity.MfaCode;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MfaCodeRepository extends JpaRepository<MfaCode,Long>{
 Optional<MfaCode> findTopByUserIdAndUsedFalseOrderByIdDesc(Long userId);
 List<MfaCode> findByUserIdAndUsedFalse(Long userId);
}
