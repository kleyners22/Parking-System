package com.parking.repo;
import com.parking.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
public interface TransactionRepository extends JpaRepository<ParkingTransaction, Long> {
  Optional<ParkingTransaction> findByCode(String code);
  boolean existsByPlateIgnoreCaseAndStatus(String plate, TxStatus status);
  long countByLocationIdAndVehicleTypeIdAndStatus(Integer locationId, Integer vtId, TxStatus status);
  List<ParkingTransaction> findByStatusOrderByEntryTimeDesc(TxStatus status);
}
