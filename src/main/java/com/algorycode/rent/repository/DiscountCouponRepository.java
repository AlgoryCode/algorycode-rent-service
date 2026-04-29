package com.algorycode.rent.repository;

import com.algorycode.rent.domain.coupon.DiscountCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscountCouponRepository extends JpaRepository<DiscountCoupon, Long> {

  Optional<DiscountCoupon> findByCodeIgnoreCase(String code);
}
