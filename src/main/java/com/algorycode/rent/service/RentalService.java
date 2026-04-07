package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.CreateRentalRequest;
import com.algorycode.rent.api.dto.RentalDto;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ConflictException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.api.mapper.RentalMapper;
import com.algorycode.rent.domain.rental.CustomerSnapshot;
import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.rental.RentalStatus;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.repository.RentalRepository;
import com.algorycode.rent.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class RentalService {

  private final RentalRepository rentalRepository;
  private final VehicleRepository vehicleRepository;

  public RentalService(RentalRepository rentalRepository, VehicleRepository vehicleRepository) {
    this.rentalRepository = rentalRepository;
    this.vehicleRepository = vehicleRepository;
  }

  @Transactional(readOnly = true)
  public List<RentalDto> list(UUID vehicleId, RentalStatus status) {
    if (vehicleId != null && status != null) {
      return rentalRepository.findByVehicle_IdAndStatusOrderByCreatedAtDesc(vehicleId, status).stream()
          .map(RentalMapper::toDto)
          .toList();
    }
    if (vehicleId != null) {
      return rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(vehicleId).stream()
          .map(RentalMapper::toDto)
          .toList();
    }
    if (status != null) {
      return rentalRepository.findByStatusOrderByCreatedAtDesc(status).stream()
          .map(RentalMapper::toDto)
          .toList();
    }
    return rentalRepository.findAllByOrderByCreatedAtDesc().stream().map(RentalMapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public RentalDto getById(UUID id) {
    var r =
        rentalRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rental not found: " + id));
    return RentalMapper.toDto(r);
  }

  @Transactional
  public RentalDto create(CreateRentalRequest req) {
    if (req.endDate().isBefore(req.startDate())) {
      throw new BadRequestException("Bitiş tarihi başlangıçtan önce olamaz.");
    }
    Vehicle vehicle =
        vehicleRepository
            .findById(req.vehicleId())
            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + req.vehicleId()));
    if (vehicle.isMaintenance()) {
      throw new ConflictException("Bakımdaki araç kiralanamaz.");
    }
    RentalStatus status = req.status() != null ? req.status() : RentalStatus.active;
    List<Rental> sameVehicle = rentalRepository.findByVehicle_IdOrderByCreatedAtDesc(req.vehicleId());
    for (Rental r : sameVehicle) {
      if (r.getStatus() == RentalStatus.cancelled) {
        continue;
      }
      if (datesOverlap(r.getStartDate(), r.getEndDate(), req.startDate(), req.endDate())) {
        throw new ConflictException("Bu tarih aralığında zaten bir kiralama var.");
      }
    }
    Rental rental = new Rental();
    rental.setVehicle(vehicle);
    rental.setStartDate(req.startDate());
    rental.setEndDate(req.endDate());
    rental.setStatus(status);
    CustomerSnapshot c = new CustomerSnapshot();
    c.setFullName(req.customer().fullName().trim());
    c.setNationalId(req.customer().nationalId().trim());
    c.setPassportNo(req.customer().passportNo().trim());
    c.setPhone(req.customer().phone().trim());
    rental.setCustomer(c);
    return RentalMapper.toDto(rentalRepository.save(rental));
  }

  private static boolean datesOverlap(LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {
    return !aStart.isAfter(bEnd) && !bStart.isAfter(aEnd);
  }
}
