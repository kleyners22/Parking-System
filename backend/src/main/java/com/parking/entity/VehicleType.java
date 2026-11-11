package com.parking.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "vehicle_types") @Getter @Setter
public class VehicleType {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
  @Column(unique = true, nullable = false) private String code;   // CAR / MOTO
  @Column(nullable = false) private String type;
  private String description;
}
