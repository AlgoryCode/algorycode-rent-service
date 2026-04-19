package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.report.RentalDashboardReportDto;
import com.algorycode.rent.service.RentalReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
public class ReportController {

  private final RentalReportService rentalReportService;

  public ReportController(RentalReportService rentalReportService) {
    this.rentalReportService = rentalReportService;
  }

  /**
   * Panel raporlari: toplam / arac bazli / zamansal (gun veya ay) kiralama ve gelir (EUR).
   *
   * @param from baslangic (bos: bitisten 1 ay once)
   * @param to bitis dahil (bos: bugun)
   * @param vehicleId filtre (bos: tum filo)
   */
  @GetMapping("/rentals/dashboard")
  public RentalDashboardReportDto rentalDashboard(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(required = false) UUID vehicleId) {
    return rentalReportService.rentalDashboard(from, to, vehicleId);
  }
}
