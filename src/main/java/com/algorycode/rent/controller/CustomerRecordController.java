package com.algorycode.rent.controller;

import com.algorycode.rent.dto.CustomerRecordDeletionDto;
import com.algorycode.rent.dto.CustomerRecordStateDto;
import com.algorycode.rent.dto.PatchCustomerRecordRequest;
import com.algorycode.rent.service.CustomerRecordService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer-records")
@Validated
@RequiredArgsConstructor
public class CustomerRecordController {

  private final CustomerRecordService customerRecordService;

  @GetMapping
  public List<CustomerRecordStateDto> list() {
    return customerRecordService.listAll();
  }

  @PatchMapping("/{recordKey}")
  public CustomerRecordStateDto patchActive(
      @PathVariable("recordKey") String recordKey,
      @Valid @RequestBody PatchCustomerRecordRequest body) {
    return customerRecordService.setActive(recordKey, body.active());
  }

  @DeleteMapping("/{recordKey}")
  public CustomerRecordDeletionDto delete(@PathVariable("recordKey") String recordKey) {
    return customerRecordService.deleteCustomerData(recordKey);
  }
}
