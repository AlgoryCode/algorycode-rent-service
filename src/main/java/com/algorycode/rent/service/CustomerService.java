package com.algorycode.rent.service;

import com.algorycode.rent.dto.CustomerRequest;
import com.algorycode.rent.dto.CustomerResponse;
import com.algorycode.rent.exception.ConflictException;
import com.algorycode.rent.exception.ResourceNotFoundException;
import com.algorycode.rent.mapper.CustomerMapper;
import com.algorycode.rent.entity.Customer;
import com.algorycode.rent.repository.CustomerRepository;
import com.algorycode.rent.repository.RentalRepository;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

  private final CustomerRepository customerRepository;
  private final RentalRepository rentalRepository;
  private final ObjectStorageService objectStorageService;

  @Transactional(readOnly = true)
  public Page<CustomerResponse> list(Pageable pageable) {
    return customerRepository.findAll(pageable).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public CustomerResponse getById(Long id) {
    Customer c =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    return toResponse(c);
  }

  @Transactional
  public CustomerResponse create(CustomerRequest req) {
    ensureUniqueEmailForStandaloneCreate(req);
    Customer c =
        Customer.builder()
            .fullName("")
            .nationalId("")
            .passportNo("")
            .phone("")
            .build();
    CustomerMapper.applyCreate(c, req);
    return toResponse(customerRepository.save(c));
  }

  @Transactional
  public CustomerResponse update(Long id, CustomerRequest req) {
    Customer c =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    rejectDuplicateEmailOnUpdate(id, req);
    CustomerMapper.mergeScalars(c, req);
    applyImageUploads(c, req, "customers/" + c.getId());
    return toResponse(customerRepository.save(c));
  }

  @Transactional
  public void delete(Long id) {
    if (!customerRepository.existsById(id)) {
      throw new ResourceNotFoundException("Customer not found: " + id);
    }
    if (rentalRepository.countByCustomerId(id) > 0) {
      throw new ConflictException("Bu müşteriye bağlı kiralamalar var; silinemez.");
    }
    customerRepository.deleteById(id);
  }

  @Transactional
  public Customer createCustomer(CustomerRequest req) {
    String nationalId = req.nationalId() != null ? req.nationalId().trim() : "";
    String phone = req.phone().trim();
    Optional<Customer> existing =
        nationalId.isBlank()
            ? customerRepository.findFirstByPhoneOrderByIdAsc(phone)
            : customerRepository.findFirstByNationalIdIgnoreCaseOrderByIdAsc(nationalId);
    rejectDuplicateEmailOnCreate(existing, req);
    Customer c =
        existing.orElseGet(
            () ->
                Customer.builder()
                    .fullName("")
                    .nationalId("")
                    .passportNo("")
                    .phone("")
                    .build());
    CustomerMapper.applyCreate(c, req);
    return customerRepository.save(c);
  }

  @Transactional
  public void updateCustomer(Customer c, CustomerRequest req, Long rentalId) {
    rejectDuplicateEmailOnUpdate(c.getId(), req);
    CustomerMapper.mergeScalars(c, req);
    applyImageUploads(c, req, "rentals/" + rentalId + "/customer");
    customerRepository.save(c);
  }

  private void ensureUniqueEmailForStandaloneCreate(CustomerRequest req) {
    if (isEmailRegistered(normalizedEmail(req.email()))) {
      throw new ConflictException("Bu e-posta adresi ile kayıtlı bir müşteri zaten var.");
    }
  }

  private boolean isEmailRegistered(String normalizedEmail) {
    if (normalizedEmail == null) {
      return false;
    }
    return customerRepository.existsByEmailIgnoreCase(normalizedEmail);
  }

  private void rejectDuplicateEmailOnCreate(
      Optional<Customer> identityMatch, CustomerRequest req) {
    String email = normalizedEmail(req.email());
    if (email == null) {
      return;
    }
    Optional<Long> selfId = identityMatch.map(Customer::getId);
    if (selfId.isEmpty()) {
      if (customerRepository.existsByEmailIgnoreCase(email)) {
        throw new ConflictException("Bu e-posta adresi ile kayıtlı bir müşteri zaten var.");
      }
      return;
    }
    if (customerRepository.existsByEmailIgnoreCaseAndIdNot(email, selfId.get())) {
      throw new ConflictException("Bu e-posta adresi ile kayıtlı bir müşteri zaten var.");
    }
  }

  private void rejectDuplicateEmailOnUpdate(Long excludeCustomerId, CustomerRequest req) {
    String email = normalizedEmail(req.email());
    if (email == null) {
      return;
    }
    if (customerRepository.existsByEmailIgnoreCaseAndIdNot(email, excludeCustomerId)) {
      throw new ConflictException("Bu e-posta adresi başka bir müşteriye ait.");
    }
  }

  private static String normalizedEmail(String raw) {
    if (raw == null) {
      return null;
    }
    String t = raw.trim();
    if (t.isEmpty()) {
      return null;
    }
    return t.toLowerCase(Locale.ROOT);
  }

  private void applyImageUploads(Customer c, CustomerRequest req, String baseScopePath) {
    if (req.passportImageDataUrl() != null && !req.passportImageDataUrl().isBlank()) {
      c.setPassportImageDataUrl(
          objectStorageService.uploadDataUrl(
              baseScopePath + "/passport",
              "passport",
              req.passportImageDataUrl().trim()));
    }
    if (req.driverLicenseImageDataUrl() != null
        && !req.driverLicenseImageDataUrl().isBlank()) {
      c.setDriverLicenseImageDataUrl(
          objectStorageService.uploadDataUrl(
              baseScopePath + "/license",
              "license",
              req.driverLicenseImageDataUrl().trim()));
    }
  }

  private CustomerResponse toResponse(Customer c) {
    return CustomerMapper.toResponse(c, objectStorageService::resolvePublicUrl);
  }
}
