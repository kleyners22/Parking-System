package com.parking.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity @Table(name = "tariffs") @Getter @Setter
public class Tariff {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
  @Column(unique = true, nullable = false) private String code;
  @Enumerated(EnumType.STRING) @Column(nullable = false) private TariffType type;
  @Column(name = "hourly_value", nullable = false) private BigDecimal hourlyValue;
  @Column(name = "max_value") private BigDecimal maxValue;
  private String description;
  @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "created_by") private User createdBy; // RF-19
}
