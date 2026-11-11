package com.parking.entity;
import jakarta.persistence.*;
import java.time.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity @Table(name = "users") @Getter @Setter
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
  @Column(unique = true, nullable = false) private String username;
  @Column(name = "password_hash", nullable = false) private String passwordHash;
  @Column(unique = true, nullable = false) private String email;
  private String fullName;
  private String phone;
  private String address;
  private LocalDate birthDate;
  private String gender;
  @Column(columnDefinition = "TEXT") private String photoUrl;
  @Enumerated(EnumType.STRING) @Column(nullable = false) private Role role;
  @Column(nullable = false) private boolean active = true;          // RF-06
  @CreationTimestamp private LocalDateTime createdAt;               // RF-07
  @UpdateTimestamp  private LocalDateTime updatedAt;
}
