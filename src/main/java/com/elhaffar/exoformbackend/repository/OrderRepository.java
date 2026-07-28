package com.elhaffar.exoformbackend.repository;

import com.elhaffar.exoformbackend.common.enums.OrderStatus;
import com.elhaffar.exoformbackend.entities.Order;
import com.elhaffar.exoformbackend.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Page<Order> findByUser(User user, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    // Rapport ventes — agrégat global (somme CA + nombre commandes) sur statuts CONFIRMED/SHIPPED/DELIVERED
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0), COUNT(o) FROM Order o " +
            "WHERE o.status IN :statuses AND o.createdAt >= :start AND o.createdAt < :end")
    List<Object[]> findRevenueAggregate(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Rapport ventes — CA et nombre de commandes par jour, trié par date croissante
    @Query("SELECT FUNCTION('DATE', o.createdAt), SUM(o.totalAmount), COUNT(o) FROM Order o " +
           "WHERE o.status IN :statuses AND o.createdAt >= :start AND o.createdAt < :end " +
           "GROUP BY FUNCTION('DATE', o.createdAt) ORDER BY FUNCTION('DATE', o.createdAt) ASC")
    List<Object[]> findDailyRevenue(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Rapport ventes — nombre de commandes par statut (tous statuts) sur la période
    @Query("SELECT o.status, COUNT(o) FROM Order o " +
           "WHERE o.createdAt >= :start AND o.createdAt < :end GROUP BY o.status")
    List<Object[]> findOrderCountByStatus(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Rapport ventes — top 5 produits par quantité vendue sur les commandes CONFIRMED/SHIPPED/DELIVERED
    @Query("SELECT oi.product.id, oi.productName, SUM(oi.quantity), SUM(oi.unitPrice * oi.quantity) " +
           "FROM OrderItem oi " +
           "WHERE oi.order.status IN :statuses AND oi.order.createdAt >= :start AND oi.order.createdAt < :end " +
           "GROUP BY oi.product.id, oi.productName ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopProducts(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);
}
