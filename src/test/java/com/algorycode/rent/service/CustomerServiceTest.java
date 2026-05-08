package com.algorycode.rent.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.algorycode.rent.dto.CustomerRequest;
import com.algorycode.rent.exception.ConflictException;
import com.algorycode.rent.exception.ResourceNotFoundException;
import com.algorycode.rent.entity.Customer;
import com.algorycode.rent.repository.CustomerRepository;
import com.algorycode.rent.repository.RentalRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

  @Mock private CustomerRepository customerRepository;
  @Mock private RentalRepository rentalRepository;
  @Mock private ObjectStorageService objectStorageService;

  @InjectMocks private CustomerService customerService;

  @Test
  void delete_whenMissing_thenNotFound() {
    when(customerRepository.existsById(99L)).thenReturn(false);

    assertThatThrownBy(() -> customerService.delete(99L))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(customerRepository, never()).deleteById(anyLong());
  }

  @Test
  void delete_whenHasRentals_thenConflict() {
    when(customerRepository.existsById(1L)).thenReturn(true);
    when(rentalRepository.countByCustomerId(1L)).thenReturn(1L);

    assertThatThrownBy(() -> customerService.delete(1L)).isInstanceOf(ConflictException.class);
    verify(customerRepository, never()).deleteById(anyLong());
  }

  @Test
  void delete_whenNoRentals_thenDeletes() {
    when(customerRepository.existsById(1L)).thenReturn(true);
    when(rentalRepository.countByCustomerId(1L)).thenReturn(0L);

    customerService.delete(1L);

    verify(customerRepository).deleteById(1L);
  }

  @Test
  void createCustomer_whenEmailBelongsToAnother_thenConflict() {
    when(customerRepository.findFirstByPhoneOrderByIdAsc("+90")).thenReturn(Optional.empty());
    when(customerRepository.existsByEmailIgnoreCase("dup@x.com")).thenReturn(true);

    var req =
        new CustomerRequest(
            "Ali", "", "", "+90", "dup@x.com", null, null, null, null);

    assertThatThrownBy(() -> customerService.createCustomer(req)).isInstanceOf(ConflictException.class);
    verify(customerRepository, never()).save(any());
  }

  @Test
  void createCustomer_whenEmailMatchesIdentityRow_thenSaves() {
    Customer existing =
        Customer.builder().fullName("a").nationalId("").passportNo("").phone("+90").build();
    existing.setId(5L);
    when(customerRepository.findFirstByPhoneOrderByIdAsc("+90")).thenReturn(Optional.of(existing));
    when(customerRepository.existsByEmailIgnoreCaseAndIdNot("x@y.com", 5L)).thenReturn(false);
    when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

    var req =
        new CustomerRequest(
            "Ali", "", "", "+90", "x@y.com", null, null, null, null);

    customerService.createCustomer(req);

    verify(customerRepository).save(any(Customer.class));
  }

  @Test
  void create_whenDuplicateEmail_thenConflict() {
    when(customerRepository.existsByEmailIgnoreCase("a@b.com")).thenReturn(true);
    var req =
        new CustomerRequest(
            "Ali", "1", "", "+90", "a@b.com", null, null, null, null);

    assertThatThrownBy(() -> customerService.create(req))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("e-posta");
    verify(customerRepository, never()).save(any());
  }
}
