package com.algorycode.rent.repository;

import com.algorycode.rent.domain.coupon.DiscountCoupon;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountCouponRepository extends JpaRepository<DiscountCoupon, Long> {

  Optional<DiscountCoupon> findByCodeIgnoreCase(String code);
}
