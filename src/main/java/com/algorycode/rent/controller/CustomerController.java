package com.algorycode.rent.controller;

import com.algorycode.rent.dto.CustomerRequest;
import com.algorycode.rent.dto.CustomerResponse;
import com.algorycode.rent.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

  private final CustomerService customerService;

  @GetMapping
  public Page<CustomerResponse> list(
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return customerService.list(pageable);
  }

  @GetMapping("/{id}")
  public CustomerResponse get(@PathVariable Long id) {
    return customerService.getById(id);
  }

  @PostMapping
  public CustomerResponse create(@Valid @RequestBody CustomerRequest body) {
    return customerService.create(body);
  }

  @PatchMapping("/{id}")
  public CustomerResponse update(@PathVariable Long id, @RequestBody CustomerRequest body) {
    return customerService.update(id, body);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    customerService.delete(id);
  }
}
