package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.CustomerRecordDeletionDto;
import com.algorycode.rent.api.dto.CustomerRecordStateDto;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.domain.customer.CustomerRecordKeys;
import com.algorycode.rent.domain.customer.CustomerRecordState;
import com.algorycode.rent.domain.rental.CustomerSnapshot;
import com.algorycode.rent.domain.request.RentalRequestCustomerSnapshot;
import com.algorycode.rent.repository.CustomerRecordStateRepository;
import com.algorycode.rent.repository.RentalRepository;
import com.algorycode.rent.repository.RentalRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerRecordService {

  private static final int MAX_KEY_LEN = 255;

  private final CustomerRecordStateRepository stateRepository;
  private final RentalRepository rentalRepository;
  private final RentalRequestRepository rentalRequestRepository;

  public CustomerRecordService(
      CustomerRecordStateRepository stateRepository,
      RentalRepository rentalRepository,
      RentalRequestRepository rentalRequestRepository) {
    this.stateRepository = stateRepository;
    this.rentalRepository = rentalRepository;
    this.rentalRequestRepository = rentalRequestRepository;
  }

  @Transactional(readOnly = true)
  public List<CustomerRecordStateDto> listAll() {
    return stateRepository.findAllByDeletedFalseOrderByRecordKeyAsc().stream()
        .map(s -> new CustomerRecordStateDto(s.getRecordKey(), s.isActive()))
        .toList();
  }

  @Transactional
  public CustomerRecordStateDto setActive(String recordKey, boolean active) {
    validateRecordKey(recordKey);
    CustomerRecordState row = stateRepository.findById(recordKey).orElseGet(CustomerRecordState::new);
    if (row.getRecordKey() != null && row.isDeleted()) {
      throw new BadRequestException("Silinmiş müşteri kaydı güncellenemez.");
    }
    row.setRecordKey(recordKey);
    row.setActive(active);
    row.setDeleted(false);
    row = stateRepository.save(row);
    return new CustomerRecordStateDto(row.getRecordKey(), row.isActive());
  }

  /**
   * Pasif müşteri için yeni kiralama / talep veya müşteri bilgisinin güncellenmesini engeller.
   */
  public void assertCustomerActive(String recordKey) {
    if (recordKey == null || recordKey.isBlank()) {
      return;
    }
    stateRepository
        .findById(recordKey)
        .ifPresent(
            s -> {
              if (s.isDeleted()) {
                throw new BadRequestException("Bu müşteri silinmiş; bu işlem yapılamaz.");
              }
              if (!s.isActive()) {
                throw new BadRequestException("Bu müşteri pasif; bu işlem yapılamaz.");
              }
            });
  }

  public void assertCustomerActive(CustomerSnapshot snapshot) {
    assertCustomerActive(CustomerRecordKeys.fromRentalCustomer(snapshot));
  }

  public void assertCustomerActive(RentalRequestCustomerSnapshot snapshot) {
    assertCustomerActive(CustomerRecordKeys.fromRequestCustomer(snapshot));
  }

  /**
   * {@code tc:} / {@code ph:} anahtarlarına uyan tüm kiralama ve talep kayıtlarını siler; {@code manual:}
   * için yalnızca sunucudaki durum satırı silinir (tarayıcıdaki manuel liste FE tarafında temizlenir).
   */
  @Transactional
  public CustomerRecordDeletionDto deleteCustomerData(String recordKey) {
    validateRecordKey(recordKey);
    if (recordKey.startsWith("manual:")) {
      softDeleteStateIfPresent(recordKey);
      return new CustomerRecordDeletionDto(0, 0);
    }
    if (!recordKey.startsWith("tc:") && !recordKey.startsWith("ph:")) {
      throw new BadRequestException("Geçersiz müşteri anahtarı.");
    }
    List<Long> rentalIds = rentalRepository.findIdsByCustomerRecordKey(recordKey);
    List<Long> requestIds = rentalRequestRepository.findIdsByCustomerRecordKey(recordKey);
    for (Long id : rentalIds) {
      rentalRepository.findById(id).ifPresent(rentalRepository::delete);
    }
    for (Long id : requestIds) {
      rentalRequestRepository.findById(id).ifPresent(rentalRequestRepository::delete);
    }
    softDeleteStateIfPresent(recordKey);
    return new CustomerRecordDeletionDto(rentalIds.size(), requestIds.size());
  }

  private void softDeleteStateIfPresent(String recordKey) {
    stateRepository
        .findById(recordKey)
        .ifPresent(
            s -> {
              s.setDeleted(true);
              stateRepository.save(s);
            });
  }

  private static void validateRecordKey(String recordKey) {
    if (recordKey == null || recordKey.isBlank()) {
      throw new BadRequestException("Müşteri anahtarı boş olamaz.");
    }
    if (recordKey.length() > MAX_KEY_LEN) {
      throw new BadRequestException("Müşteri anahtarı çok uzun.");
    }
    if (!(recordKey.startsWith("tc:")
        || recordKey.startsWith("ph:")
        || recordKey.startsWith("manual:"))) {
      throw new BadRequestException("Geçersiz müşteri anahtarı.");
    }
  }
}
