package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.CreateCouponRequest;
import com.algorycode.rent.api.dto.DiscountCouponDto;
import com.algorycode.rent.api.dto.UpdateCouponRequest;
import com.algorycode.rent.api.dto.ValidateCouponResponse;
import com.algorycode.rent.service.DiscountCouponService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/coupons")
public class DiscountCouponController {

  private final DiscountCouponService discountCouponService;

  public DiscountCouponController(DiscountCouponService discountCouponService) {
    this.discountCouponService = discountCouponService;
  }

  @GetMapping
  public List<DiscountCouponDto> list() {
    return discountCouponService.list();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public DiscountCouponDto create(@Valid @RequestBody CreateCouponRequest req) {
    return discountCouponService.create(req);
  }

  @PutMapping("/{id}")
  public DiscountCouponDto update(@PathVariable Long id, @Valid @RequestBody UpdateCouponRequest req) {
    return discountCouponService.update(id, req);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    discountCouponService.delete(id);
  }

  @GetMapping("/validate")
  public ValidateCouponResponse validate(@RequestParam String code) {
    return discountCouponService.validate(code);
  }
}
