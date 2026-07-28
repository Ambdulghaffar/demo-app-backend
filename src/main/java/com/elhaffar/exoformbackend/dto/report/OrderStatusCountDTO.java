package com.elhaffar.exoformbackend.dto.report;

import com.elhaffar.exoformbackend.common.enums.OrderStatus;

public record OrderStatusCountDTO(OrderStatus status, long count) {}
