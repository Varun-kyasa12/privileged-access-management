package com.pam.pam_backend.repository;
import java.util.List;
import com.pam.pam_backend.entity.Application;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface ApplicationRepository extends JpaRepository<Application,Long>{
 @Query(value="SELECT p.name FROM permissions p JOIN permission_applications pa ON pa.permission_id=p.id WHERE pa.application_id=:id ORDER BY p.name",nativeQuery=true)
 List<String> permissionNames(@Param("id") Long id);
}