package com.elhaffar.exoformbackend.repository;

import com.elhaffar.exoformbackend.entities.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Integer> {
}
