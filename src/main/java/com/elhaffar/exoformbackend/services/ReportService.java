package com.elhaffar.exoformbackend.services;

import com.elhaffar.exoformbackend.dto.report.SalesReportDTO;
import com.elhaffar.exoformbackend.dto.report.StockReportDTO;

import java.time.LocalDate;

public interface ReportService {

    SalesReportDTO getSalesReport(LocalDate startDate, LocalDate endDate);

    StockReportDTO getStockReport(LocalDate startDate, LocalDate endDate);
}
