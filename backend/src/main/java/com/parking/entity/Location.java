package com.parking.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "locations") @Getter @Setter
public class Location {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
  @Column(unique = true, nullable = false) private String code;
  @Column(nullable = false) private String name;
  private String address;
  @Column(columnDefinition = "TEXT") private String notes;
  @Column(name = "lot_mobil", nullable = false) private int lotMobil;  // autos
  @Column(name = "lot_motor", nullable = false) private int lotMotor;  // motos
}
