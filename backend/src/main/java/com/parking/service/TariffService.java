package com.parking.service;
import com.parking.entity.*;
import com.parking.repo.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TariffService {
  private final TariffRepository tariffs;
  private final MemberRepository members;
  public TariffService(TariffRepository t, MemberRepository m) { tariffs = t; members = m; }

  /** RF-35: busca una membresia vigente para la placa en la ubicacion indicada. */
  public Optional<Member> findActiveMember(String plate, Integer locationId, LocalDate on) {
    return members.findActiveByPlate(plate).stream()
        .filter(m -> m.getLocation().getId().equals(locationId) && m.isValidOn(on))
        .findFirst();
  }

  /** Tarifa aplicable: Miembro/VIP si hay membresia vigente; si no, Regular. */
  public Tariff resolveTariff(Optional<Member> member) {
    TariffType type = member.map(Member::getCategory).orElse(TariffType.REGULAR);
    return tariffs.findFirstByType(type).or(() -> tariffs.findFirstByType(TariffType.REGULAR))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No hay tarifa configurada"));
  }

  /** RF-28: minutos de permanencia. */
  public long minutes(LocalDateTime in, LocalDateTime out) {
    return Math.max(0, Duration.between(in, out).toMinutes());
  }

  /** RF-29: Tarifa = horas (fraccion = hora completa, minimo 1) x valor hora, con tope maximo. */
  public BigDecimal calculate(long minutes, Tariff t) {
    long hours = Math.max(1, (long) Math.ceil(minutes / 60.0));
    BigDecimal total = t.getHourlyValue().multiply(BigDecimal.valueOf(hours));
    if (t.getMaxValue() != null && total.compareTo(t.getMaxValue()) > 0) total = t.getMaxValue();
    return total;
  }
}
