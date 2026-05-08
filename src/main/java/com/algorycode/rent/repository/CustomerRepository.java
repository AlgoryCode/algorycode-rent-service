package com.algorycode.rent.repository;

import com.algorycode.rent.entity.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

  Optional<Customer> findFirstByNationalIdIgnoreCaseOrderByIdAsc(String nationalId);

  Optional<Customer> findFirstByPhoneOrderByIdAsc(String phone);

  boolean existsByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCaseAndIdNot(String email, Long excludeCustomerId);
}
