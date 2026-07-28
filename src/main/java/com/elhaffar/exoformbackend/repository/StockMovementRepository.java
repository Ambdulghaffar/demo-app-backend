package com.elhaffar.exoformbackend.repository;

import com.elhaffar.exoformbackend.common.enums.StockMovementType;
import com.elhaffar.exoformbackend.entities.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Integer> {

    @Query(value = """
            SELECT sm FROM StockMovement sm
            JOIN FETCH sm.product p
            WHERE (:productId IS NULL OR p.id = :productId)
            AND (:type IS NULL OR sm.type = :type)
            ORDER BY sm.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(sm) FROM StockMovement sm
            JOIN sm.product p
            WHERE (:productId IS NULL OR p.id = :productId)
            AND (:type IS NULL OR sm.type = :type)
            """)
    Page<StockMovement> findWithFilters(
            @Param("productId") Integer productId,
            @Param("type") StockMovementType type,
            Pageable pageable
    );

    // Rapport stock — résumé des mouvements par type (count + quantité totale) sur la période
    @Query("SELECT sm.type, COUNT(sm), SUM(sm.quantity) FROM StockMovement sm " +
           "WHERE sm.createdAt >= :start AND sm.createdAt < :end GROUP BY sm.type")
    List<Object[]> findMovementSummaryByType(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
