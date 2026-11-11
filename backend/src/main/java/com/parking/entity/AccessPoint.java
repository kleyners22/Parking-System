package com.parking.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "access_points") @Getter @Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "createdBy"})
public class AccessPoint {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
  @Column(unique = true, nullable = false) private String code;
  @Column(nullable = false) private String name;
  private String type;
  private String description;
  @ManyToOne(optional = false, fetch = FetchType.EAGER) private VehicleType vehicleType;  // RF-14
  @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "created_by") private User createdBy; // RF-15
}
