package com.elhaffar.exoformbackend.repository;

import com.elhaffar.exoformbackend.common.enums.SupplierOrderStatus;
import com.elhaffar.exoformbackend.entities.Supplier;
import com.elhaffar.exoformbackend.entities.SupplierOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierOrderRepository extends JpaRepository<SupplierOrder, Integer> {

    Page<SupplierOrder> findBySupplier(Supplier supplier, Pageable pageable);

    Page<SupplierOrder> findByStatus(SupplierOrderStatus status, Pageable pageable);
}
