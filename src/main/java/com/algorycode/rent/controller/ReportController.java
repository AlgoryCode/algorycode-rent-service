package com.algorycode.rent.controller;

import com.algorycode.rent.dto.report.RentalDashboardReportDto;
import com.algorycode.rent.service.RentalReportService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

  private final RentalReportService rentalReportService;

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
      @RequestParam(required = false) Long vehicleId) {
    return rentalReportService.rentalDashboard(from, to, vehicleId);
  }
}
