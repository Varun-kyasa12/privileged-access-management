package com.pam.pam_backend.service;
import java.time.LocalDateTime;
import com.pam.pam_backend.entity.AuditLog;
import com.pam.pam_backend.repository.AuditLogRepository;
import com.pam.pam_backend.exception.ApiException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.web.context.request.*;
@Service
public class AuditLogService {
 private final AuditLogRepository repository;
 public AuditLogService(AuditLogRepository repository){this.repository=repository;}
 // Permission decisions are recorded before business transactions; MFA persists rejected attempts.
 @Transactional
 public void record(Long userId,String action,String resource,String status){
  AuditLog log=new AuditLog();log.setUserId(userId);log.setAction(action);log.setResource(resource);
  log.setStatus(status);log.setCreatedAt(LocalDateTime.now());
  if(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes r)log.setIpAddress(r.getRequest().getRemoteAddr());
  repository.save(log);
 }
 public Page<AuditLog> list(int page,int size){
  if(page<0 || size<1 || size>100)throw new ApiException(400,"Use page >= 0 and size between 1 and 100");
  return repository.findAll(PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"id")));
 }
}