package com.algorycode.rent.repository;

import com.algorycode.rent.entity.RentalRequestPricedLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRequestPricedLineRepository
    extends JpaRepository<RentalRequestPricedLine, Long> {}
