package com.parking.web;
import com.parking.entity.*;
import com.parking.repo.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class LocationController {
  private final LocationRepository locations;
  private final TransactionRepository tx;
  private final VehicleTypeRepository vehicleTypes;
  private final AccessPointRepository accessPoints;
  public LocationController(LocationRepository l, TransactionRepository t, VehicleTypeRepository v, AccessPointRepository a) {
    locations = l; tx = t; vehicleTypes = v; accessPoints = a;
  }

  // RF-08 CRUD (POST/PUT/DELETE restringidos a ADMIN en SecurityConfig)
  @GetMapping("/locations") public List<Location> list() { return locations.findAll(); }
  @PostMapping("/locations") @ResponseStatus(HttpStatus.CREATED)
  public Location create(@RequestBody Location l) { l.setId(null); return locations.save(l); }
  @PutMapping("/locations/{id}")
  public Location update(@PathVariable Integer id, @RequestBody Location l) {
    if (!locations.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    l.setId(id); return locations.save(l);
  }
  @DeleteMapping("/locations/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Integer id) { locations.deleteById(id); }

  @GetMapping("/vehicle-types") public List<VehicleType> vehicleTypes() { return vehicleTypes.findAll(); }
  @GetMapping("/access-points") public List<AccessPoint> accessPoints() { return accessPoints.findAll(); }

  /** Datos del dashboard: ocupacion por ubicacion y tipo de vehiculo (RF-09). */
  @GetMapping("/parking/availability")
  public List<Map<String, Object>> availability() {
    List<Map<String, Object>> out = new ArrayList<>();
    for (Location l : locations.findAll()) {
      for (VehicleType vt : vehicleTypes.findAll()) {
        int cap = "MOTO".equalsIgnoreCase(vt.getCode()) ? l.getLotMotor() : l.getLotMobil();
        long used = tx.countByLocationIdAndVehicleTypeIdAndStatus(l.getId(), vt.getId(), TxStatus.BERJALAN);
        out.add(Map.of("locationId", l.getId(), "location", l.getName(), "vehicleType", vt.getType(),
            "capacity", cap, "occupied", used, "free", Math.max(0, cap - used)));
      }
    }
    return out;
  }
}
