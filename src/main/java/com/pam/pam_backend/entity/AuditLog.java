package com.pam.pam_backend.entity;
import java.time.LocalDateTime;
import jakarta.persistence.*;
@Entity @Table(name="audit_logs")
public class AuditLog {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="user_id") private Long userId;
 @Column(nullable=false,length=100) private String action;
 @Column(name="application",length=100) private String resource;
 @Column(nullable=false,length=20) private String status;
 @Column(name="ip_address",length=45) private String ipAddress;
 @Column(name="created_at") private LocalDateTime createdAt;
 public Long getId(){return id;}
 public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
 public String getAction(){return action;} public void setAction(String v){action=v;}
 public String getResource(){return resource;} public void setResource(String v){resource=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
 public String getIpAddress(){return ipAddress;} public void setIpAddress(String v){ipAddress=v;}
 public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}