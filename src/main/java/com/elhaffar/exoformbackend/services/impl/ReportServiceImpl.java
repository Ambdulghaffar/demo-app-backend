package com.elhaffar.exoformbackend.services.impl;

import com.elhaffar.exoformbackend.common.enums.OrderStatus;
import com.elhaffar.exoformbackend.common.enums.StockMovementType;
import com.elhaffar.exoformbackend.dto.report.*;
import com.elhaffar.exoformbackend.entities.Product;
import com.elhaffar.exoformbackend.repository.OrderRepository;
import com.elhaffar.exoformbackend.repository.ProductRepository;
import com.elhaffar.exoformbackend.repository.StockMovementRepository;
import com.elhaffar.exoformbackend.services.ReportService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private static final List<OrderStatus> REVENUE_STATUSES =
            List.of(OrderStatus.CONFIRMED, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public ReportServiceImpl(OrderRepository orderRepository,
                             ProductRepository productRepository,
                             StockMovementRepository stockMovementRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesReportDTO getSalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDate[] dates = resolveWindow(startDate, endDate);
        LocalDateTime start = dates[0].atStartOfDay();
        LocalDateTime end   = dates[1].plusDays(1).atStartOfDay();

// Agrégat global CA + nb commandes
        List<Object[]> aggregateRows = orderRepository.findRevenueAggregate(REVENUE_STATUSES, start, end);
        Object[] aggregate = aggregateRows.isEmpty()
                ? new Object[]{ BigDecimal.ZERO, 0L }
                : aggregateRows.get(0);
        BigDecimal totalRevenue = aggregate[0] != null ? (BigDecimal) aggregate[0] : BigDecimal.ZERO;
        long totalOrders        = aggregate[1] != null ? ((Number) aggregate[1]).longValue() : 0L;
        BigDecimal averageOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // CA par jour
        List<DailyRevenueDTO> revenueByDay = orderRepository
                .findDailyRevenue(REVENUE_STATUSES, start, end)
                .stream()
                .map(row -> new DailyRevenueDTO(
                        ((Date) row[0]).toLocalDate(),
                        (BigDecimal) row[1],
                        (Long) row[2]))
                .toList();

        // Répartition par statut (tous statuts)
        List<OrderStatusCountDTO> ordersByStatus = orderRepository
                .findOrderCountByStatus(start, end)
                .stream()
                .map(row -> new OrderStatusCountDTO((OrderStatus) row[0], (Long) row[1]))
                .toList();

        // Commandes PENDING issues de la répartition par statut
        long pendingOrdersCount = ordersByStatus.stream()
                .filter(s -> s.status() == OrderStatus.PENDING)
                .mapToLong(OrderStatusCountDTO::count)
                .findFirst()
                .orElse(0L);

        // Top 5 produits par quantité vendue
        List<TopProductDTO> topProducts = orderRepository
                .findTopProducts(REVENUE_STATUSES, start, end, PageRequest.of(0, 5))
                .stream()
                .map(row -> new TopProductDTO(
                        (Integer) row[0],
                        (String)  row[1],
                        (Long)    row[2],
                        (BigDecimal) row[3]))
                .toList();

        return new SalesReportDTO(totalRevenue, totalOrders, averageOrderValue, pendingOrdersCount,
                revenueByDay, topProducts, ordersByStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public StockReportDTO getStockReport(LocalDate startDate, LocalDate endDate) {
        LocalDate[] dates = resolveWindow(startDate, endDate);
        LocalDateTime start = dates[0].atStartOfDay();
        LocalDateTime end   = dates[1].plusDays(1).atStartOfDay();

        BigDecimal rawStockValue   = productRepository.sumActiveStockValue();
        BigDecimal totalStockValue = rawStockValue != null ? rawStockValue : BigDecimal.ZERO;
        long totalProducts         = productRepository.count();
        long lowStockCount         = productRepository.countLowStockProducts();
        long outOfStockCount       = productRepository.countOutOfStockProducts();

        List<LowStockProductDTO> lowStockProducts = productRepository
                .findLowStockProducts(PageRequest.of(0, 10))
                .stream()
                .map(p -> new LowStockProductDTO(
                        p.getId(),
                        p.getName(),
                        p.getCategory().getName(),
                        p.getStock()))
                .toList();

        List<MovementTypeSummaryDTO> movementsSummary = stockMovementRepository
                .findMovementSummaryByType(start, end)
                .stream()
                .map(row -> new MovementTypeSummaryDTO(
                        (StockMovementType) row[0],
                        (Long) row[1],
                        (Long) row[2]))
                .toList();

        return new StockReportDTO(totalStockValue, totalProducts, lowStockCount, outOfStockCount,
                lowStockProducts, movementsSummary);
    }

    // Applique la fenêtre par défaut (30 derniers jours) si l'une des bornes est absente
    private LocalDate[] resolveWindow(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return new LocalDate[]{ LocalDate.now().minusDays(30), LocalDate.now() };
        }
        return new LocalDate[]{ startDate, endDate };
    }
}
