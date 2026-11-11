package com.parking.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity @Table(name = "members") @Getter @Setter
public class Member {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
  @Column(unique = true, nullable = false) private String cardNumber;
  @Column(nullable = false) private String plate;
  private String phone;
  @Column(nullable = false) private String name;
  @Enumerated(EnumType.STRING) @Column(nullable = false) private TariffType category = TariffType.MEMBER; // MEMBER|VIP
  @ManyToOne(optional = false, fetch = FetchType.LAZY) private VehicleType vehicleType;
  @ManyToOne(optional = false, fetch = FetchType.LAZY) private Location location;
  @Column(nullable = false) private LocalDate startDate;
  @Column(nullable = false) private LocalDate endDate;
  @Column(nullable = false) private boolean active = true;
  @Column(columnDefinition = "TEXT") private String notes;
  @OneToOne(fetch = FetchType.LAZY) @JoinColumn(unique = true) private User user;     // RF-24 (1:0..1)
  @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "created_by") private User createdBy; // RF-25

  public boolean isValidOn(LocalDate d) { return active && !d.isBefore(startDate) && !d.isAfter(endDate); }
}
