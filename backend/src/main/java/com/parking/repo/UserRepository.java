package com.parking.repo;
import com.parking.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
public interface UserRepository extends JpaRepository<User, Integer> {
  Optional<User> findByUsername(String username);
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
}
