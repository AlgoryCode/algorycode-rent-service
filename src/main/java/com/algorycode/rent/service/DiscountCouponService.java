package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.CreateCouponRequest;
import com.algorycode.rent.api.dto.DiscountCouponDto;
import com.algorycode.rent.api.dto.UpdateCouponRequest;
import com.algorycode.rent.api.dto.ValidateCouponResponse;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.coupon.DiscountCoupon;
import com.algorycode.rent.repository.DiscountCouponRepository;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiscountCouponService {

  private final DiscountCouponRepository discountCouponRepository;

  @Transactional(readOnly = true)
  public List<DiscountCouponDto> list() {
    return discountCouponRepository.findAll().stream().map(this::toDto).toList();
  }

  @Transactional
  public DiscountCouponDto create(CreateCouponRequest req) {
    discountCouponRepository
        .findByCodeIgnoreCase(req.code().trim())
        .ifPresent(
            c -> {
              throw new BadRequestException("Bu kupon kodu zaten kullanımda: " + req.code());
            });
    DiscountCoupon coupon = new DiscountCoupon();
    coupon.setCode(req.code().trim().toUpperCase());
    coupon.setDiscountType(req.discountType().trim().toUpperCase());
    coupon.setDiscountValue(req.discountValue().setScale(2, RoundingMode.HALF_UP));
    coupon.setDescription(req.description());
    coupon.setActive(req.active() != null ? req.active() : true);
    coupon.setUsageLimit(req.usageLimit());
    coupon.setExpiresAt(req.expiresAt());
    return toDto(discountCouponRepository.save(coupon));
  }

  @Transactional
  public DiscountCouponDto update(Long id, UpdateCouponRequest req) {
    DiscountCoupon coupon = findOrThrow(id);
    if (req.code() != null) {
      String newCode = req.code().trim().toUpperCase();
      discountCouponRepository
          .findByCodeIgnoreCase(newCode)
          .filter(c -> !c.getId().equals(id))
          .ifPresent(
              c -> {
                throw new BadRequestException("Bu kupon kodu zaten kullanımda: " + newCode);
              });
      coupon.setCode(newCode);
    }
    if (req.discountType() != null) coupon.setDiscountType(req.discountType().trim().toUpperCase());
    if (req.discountValue() != null)
      coupon.setDiscountValue(req.discountValue().setScale(2, RoundingMode.HALF_UP));
    if (req.description() != null) coupon.setDescription(req.description());
    if (req.active() != null) coupon.setActive(req.active());
    if (req.usageLimit() != null) coupon.setUsageLimit(req.usageLimit());
    if (req.expiresAt() != null) coupon.setExpiresAt(req.expiresAt());
    return toDto(discountCouponRepository.save(coupon));
  }

  @Transactional
  public void delete(Long id) {
    DiscountCoupon coupon = findOrThrow(id);
    discountCouponRepository.delete(coupon);
  }

  @Transactional(readOnly = true)
  public ValidateCouponResponse validate(String code) {
    return discountCouponRepository
        .findByCodeIgnoreCase(code.trim())
        .map(
            c -> {
              if (!c.isActive()) {
                return new ValidateCouponResponse(false, null, null, "Kupon aktif değil.");
              }
              if (c.getExpiresAt() != null && c.getExpiresAt().isBefore(Instant.now())) {
                return new ValidateCouponResponse(false, null, null, "Kuponun süresi dolmuş.");
              }
              if (c.getUsageLimit() != null && c.getUsageCount() >= c.getUsageLimit()) {
                return new ValidateCouponResponse(
                    false, null, null, "Kupon kullanım limiti doldu.");
              }
              return new ValidateCouponResponse(
                  true, c.getDiscountType(), c.getDiscountValue(), null);
            })
        .orElse(new ValidateCouponResponse(false, null, null, "Kupon bulunamadı."));
  }

  @Transactional
  public void incrementUsage(String code) {
    discountCouponRepository
        .findByCodeIgnoreCase(code.trim())
        .ifPresent(
            c -> {
              c.setUsageCount(c.getUsageCount() + 1);
              discountCouponRepository.save(c);
            });
  }

  private DiscountCoupon findOrThrow(Long id) {
    return discountCouponRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + id));
  }

  private DiscountCouponDto toDto(DiscountCoupon c) {
    return new DiscountCouponDto(
        c.getId(),
        c.getCode(),
        c.getDiscountType(),
        c.getDiscountValue(),
        c.getDescription(),
        c.isActive(),
        c.getUsageLimit(),
        c.getUsageCount(),
        c.getExpiresAt(),
        c.getCreatedAt(),
        c.getUpdatedAt());
  }
}
