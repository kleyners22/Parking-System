package com.parking.repo;
import com.parking.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
public interface TariffRepository extends JpaRepository<Tariff, Integer> {
  Optional<Tariff> findFirstByType(TariffType type);
}
