package com.algorycode.rent.controller;

import com.algorycode.rent.dto.CreateRentalRequestFormRequest;
import com.algorycode.rent.dto.RentalRequestDto;
import com.algorycode.rent.dto.UpdateRentalRequestStatusRequest;
import com.algorycode.rent.service.RentalRequestService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rental-requests")
@RequiredArgsConstructor
public class RentalRequestController {

  private final RentalRequestService rentalRequestService;

  @GetMapping
  public List<RentalRequestDto> list(@RequestParam(required = false) Long vehicleId) {
    return rentalRequestService.listAll(vehicleId);
  }

  @PostMapping
  public RentalRequestDto create(@Valid @RequestBody CreateRentalRequestFormRequest body) {
    return rentalRequestService.create(body);
  }

  @GetMapping("/reference/{referenceNo}")
  public RentalRequestDto getByReference(@PathVariable String referenceNo) {
    return rentalRequestService.getByReferenceNo(referenceNo);
  }

  @GetMapping("/{id}/contract.pdf")
  public ResponseEntity<byte[]> downloadContractPdf(@PathVariable Long id) {
    var attachment = rentalRequestService.getContractPdfAttachment(id);
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + attachment.filename() + "\"")
        .contentType(MediaType.APPLICATION_PDF)
        .contentLength(attachment.content().length)
        .body(attachment.content());
  }

  @GetMapping("/{id}")
  public RentalRequestDto getById(@PathVariable Long id) {
    return rentalRequestService.getById(id);
  }

  @PostMapping("/{id}/contract")
  public RentalRequestDto generateContract(@PathVariable Long id) {
    return rentalRequestService.generateContract(id);
  }

  /** Müşteri e-postasına sözleşme bildirimi (Thymeleaf + Rabbit mail kuyruğu). */
  @PostMapping("/{id}/send-contract-email")
  public ResponseEntity<Void> sendContractEmailToCustomer(@PathVariable Long id) {
    rentalRequestService.queueContractPdfEmailToCustomer(id);
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  @PatchMapping("/{id}/status")
  public RentalRequestDto updateStatus(
      @PathVariable Long id, @Valid @RequestBody UpdateRentalRequestStatusRequest body) {
    return rentalRequestService.updateStatus(id, body);
  }
}
