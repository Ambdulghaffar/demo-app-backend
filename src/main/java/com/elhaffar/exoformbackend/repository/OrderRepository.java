package com.elhaffar.exoformbackend.repository;

import com.elhaffar.exoformbackend.common.enums.OrderStatus;
import com.elhaffar.exoformbackend.entities.Order;
import com.elhaffar.exoformbackend.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Page<Order> findByUser(User user, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
