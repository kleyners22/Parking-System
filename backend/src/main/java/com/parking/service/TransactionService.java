package com.parking.service;
import com.parking.entity.*;
import com.parking.repo.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TransactionService {
  public record EntryRequest(String plate, Integer locationId, Integer vehicleTypeId,
                             Integer accessPointId, String entryImage) {}
  public record ExitRequest(Integer accessPointId, String exitImage) {}

  private final TransactionRepository txRepo;
  private final UserRepository users;
  private final LocationRepository locations;
  private final VehicleTypeRepository vehicleTypes;
  private final AccessPointRepository accessPoints;
  private final TariffService tariffService;
  private final QrService qr;

  public TransactionService(TransactionRepository t, UserRepository u, LocationRepository l,
      VehicleTypeRepository v, AccessPointRepository a, TariffService ts, QrService q) {
    txRepo = t; users = u; locations = l; vehicleTypes = v; accessPoints = a; tariffService = ts; qr = q;
  }

  private <T> T found(Optional<T> o, String msg) {
    return o.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, msg));
  }

  /** RF-26, RF-30, RF-31, RF-35, RF-36 */
  @Transactional
  public ParkingTransaction registerEntry(EntryRequest r, String operatorUsername) {
    String plate = r.plate().trim().toUpperCase();
    User op = found(users.findByUsername(operatorUsername), "Operador no encontrado");
    Location loc = found(locations.findById(r.locationId()), "Ubicacion no encontrada");
    VehicleType vt = found(vehicleTypes.findById(r.vehicleTypeId()), "Tipo de vehiculo no encontrado");
    AccessPoint ap = found(accessPoints.findById(r.accessPointId()), "Punto de acceso no encontrado");

    if (!ap.getVehicleType().getId().equals(vt.getId()))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El punto de acceso no corresponde al tipo de vehiculo");
    if (txRepo.existsByPlateIgnoreCaseAndStatus(plate, TxStatus.BERJALAN))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "La placa ya tiene una transaccion en curso");

    long occupied = txRepo.countByLocationIdAndVehicleTypeIdAndStatus(loc.getId(), vt.getId(), TxStatus.BERJALAN);
    int capacity = "MOTO".equalsIgnoreCase(vt.getCode()) ? loc.getLotMotor() : loc.getLotMobil();
    if (occupied >= capacity)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Parqueadero lleno para este tipo de vehiculo");

    LocalDateTime now = LocalDateTime.now();
    Optional<Member> member = tariffService.findActiveMember(plate, loc.getId(), now.toLocalDate());

    ParkingTransaction t = new ParkingTransaction();
    t.setCode("TRX-" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-"
        + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
    t.setPlate(plate);
    t.setType(TxType.MASUK);
    t.setStatus(TxStatus.BERJALAN);
    t.setEntryTime(now);
    t.setEntryAp(ap);
    t.setLocation(loc);
    t.setVehicleType(vt);
    t.setEntryImage(r.entryImage());
    t.setMember(member.orElse(null));
    t.setTariff(tariffService.resolveTariff(member));
    t.setOperator(op);
    t.setQrCode(qr.generateBase64Png(t.getCode()));
    return txRepo.save(t);
  }

  /** RF-27, RF-28, RF-29, RF-36 (la tarifa de miembro se reevalua a la hora de salida). */
  @Transactional
  public ParkingTransaction registerExit(String code, ExitRequest r, String operatorUsername) {
    ParkingTransaction t = found(txRepo.findByCode(code), "Transaccion no encontrada");
    if (t.getStatus() != TxStatus.BERJALAN)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "La transaccion no esta en curso");
    AccessPoint ap = found(accessPoints.findById(r.accessPointId()), "Punto de acceso no encontrado");
    User op = found(users.findByUsername(operatorUsername), "Operador no encontrado");

    LocalDateTime now = LocalDateTime.now();
    Optional<Member> member = tariffService.findActiveMember(t.getPlate(), t.getLocation().getId(), now.toLocalDate());
    Tariff tariff = tariffService.resolveTariff(member);
    long minutes = tariffService.minutes(t.getEntryTime(), now);

    t.setExitTime(now);
    t.setExitAp(ap);
    t.setExitImage(r.exitImage());
    t.setMember(member.orElse(null));
    t.setTariff(tariff);
    t.setDurationMin(minutes);
    t.setAmount(tariffService.calculate(minutes, tariff));
    t.setType(TxType.KELUAR);
    t.setStatus(TxStatus.SELESAI);
    t.setOperator(op);
    return txRepo.save(t);
  }

  /** RF-34 */
  @Transactional
  public ParkingTransaction cancel(String code) {
    ParkingTransaction t = found(txRepo.findByCode(code), "Transaccion no encontrada");
    if (t.getStatus() != TxStatus.BERJALAN)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden cancelar transacciones en curso");
    t.setStatus(TxStatus.DIBATALKAN);
    return txRepo.save(t);
  }

  @Transactional(readOnly = true)
  public List<ParkingTransaction> active() { return txRepo.findByStatusOrderByEntryTimeDesc(TxStatus.BERJALAN); }
}
