package com.elhaffar.exoformbackend.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyRevenueDTO(LocalDate date, BigDecimal revenue, long orderCount) {}
