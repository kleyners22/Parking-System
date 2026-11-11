package com.parking.web;
import com.parking.entity.*;
import com.parking.service.TransactionService;
import com.parking.service.TransactionService.*;
import java.security.Principal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
  private final TransactionService service;
  public TransactionController(TransactionService s) { service = s; }

  private Map<String, Object> dto(ParkingTransaction t) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("code", t.getCode()); m.put("plate", t.getPlate());
    m.put("type", t.getType()); m.put("status", t.getStatus());
    m.put("entryTime", t.getEntryTime()); m.put("exitTime", t.getExitTime());
    m.put("durationMin", t.getDurationMin()); m.put("amount", t.getAmount());
    m.put("tariffType", t.getTariff() == null ? null : t.getTariff().getType());
    m.put("isMember", t.getMember() != null);
    m.put("location", t.getLocation().getName());
    m.put("vehicleType", t.getVehicleType().getType());
    m.put("qrCode", t.getQrCode());
    return m;
  }

  @PostMapping("/entry") @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> entry(@RequestBody EntryRequest r, Principal p) {
    return dto(service.registerEntry(r, p.getName()));
  }
  @PostMapping("/{code}/exit")
  public Map<String, Object> exit(@PathVariable String code, @RequestBody ExitRequest r, Principal p) {
    return dto(service.registerExit(code, r, p.getName()));
  }
  @PostMapping("/{code}/cancel")
  public Map<String, Object> cancel(@PathVariable String code) { return dto(service.cancel(code)); }
  @GetMapping("/active")
  public List<Map<String, Object>> active() { return service.active().stream().map(this::dto).toList(); }
}
