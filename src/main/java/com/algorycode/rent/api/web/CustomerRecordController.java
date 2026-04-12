package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.CustomerRecordDeletionDto;
import com.algorycode.rent.api.dto.CustomerRecordStateDto;
import com.algorycode.rent.api.dto.PatchCustomerRecordRequest;
import com.algorycode.rent.service.CustomerRecordService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/customer-records")
@Validated
public class CustomerRecordController {

  private final CustomerRecordService customerRecordService;

  public CustomerRecordController(CustomerRecordService customerRecordService) {
    this.customerRecordService = customerRecordService;
  }

  @GetMapping
  public List<CustomerRecordStateDto> list() {
    return customerRecordService.listAll();
  }

  @PatchMapping("/{recordKey}")
  public CustomerRecordStateDto patchActive(
      @PathVariable("recordKey") String recordKey, @Valid @RequestBody PatchCustomerRecordRequest body) {
    return customerRecordService.setActive(recordKey, body.active());
  }

  @DeleteMapping("/{recordKey}")
  public CustomerRecordDeletionDto delete(@PathVariable("recordKey") String recordKey) {
    return customerRecordService.deleteCustomerData(recordKey);
  }
}
