package com.parking.repo;
import com.parking.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
public interface MemberRepository extends JpaRepository<Member, Integer> {
  @Query("select m from Member m join fetch m.location where lower(m.plate) = lower(:plate) and m.active = true")
  List<Member> findActiveByPlate(@org.springframework.data.repository.query.Param("plate") String plate);
}
