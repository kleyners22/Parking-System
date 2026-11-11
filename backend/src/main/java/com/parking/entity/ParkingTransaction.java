package com.parking.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity @Table(name = "transactions") @Getter @Setter
public class ParkingTransaction {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(unique = true, nullable = false) private String code;                       // RF-30
  @Column(nullable = false) private String plate;
  @Enumerated(EnumType.STRING) @Column(nullable = false) private TxType type;          // RF-33
  @Enumerated(EnumType.STRING) @Column(nullable = false) private TxStatus status;      // RF-32
  @Column(nullable = false) private LocalDateTime entryTime;
  private LocalDateTime exitTime;
  @ManyToOne(optional = false, fetch = FetchType.LAZY) private AccessPoint entryAp;
  @ManyToOne(fetch = FetchType.LAZY) private AccessPoint exitAp;
  @ManyToOne(optional = false, fetch = FetchType.EAGER) private Location location;
  @ManyToOne(optional = false, fetch = FetchType.EAGER) private VehicleType vehicleType;
  @ManyToOne(fetch = FetchType.EAGER) private Tariff tariff;
  @ManyToOne(fetch = FetchType.LAZY) private Member member;
  @Column(columnDefinition = "TEXT") private String entryImage;
  @Column(columnDefinition = "TEXT") private String exitImage;
  private Long durationMin;                                                              // RF-28
  private BigDecimal amount;                                                             // RF-29
  @Column(columnDefinition = "TEXT") private String qrCode;                            // RF-31
  @ManyToOne(optional = false, fetch = FetchType.LAZY) private User operator;          // RF-36
}
