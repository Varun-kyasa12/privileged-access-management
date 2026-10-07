package com.pam.pam_backend.entity;
import jakarta.persistence.*;
@Entity @Table(name="applications")
public class Application {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private String name;private String description;
 public Long getId(){return id;}public String getName(){return name;}public String getDescription(){return description;}
}