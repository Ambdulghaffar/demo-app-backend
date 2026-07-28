package com.elhaffar.exoformbackend.dto.report;

import java.math.BigDecimal;
import java.util.List;

public record SalesReportDTO(
        BigDecimal totalRevenue,
        long totalOrders,
        BigDecimal averageOrderValue,
        long pendingOrdersCount,
        List<DailyRevenueDTO> revenueByDay,
        List<TopProductDTO> topProducts,
        List<OrderStatusCountDTO> ordersByStatus
) {}
