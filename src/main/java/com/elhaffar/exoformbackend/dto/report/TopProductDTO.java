package com.elhaffar.exoformbackend.dto.report;

import java.math.BigDecimal;

public record TopProductDTO(Integer productId, String productName, long quantitySold, BigDecimal revenue) {}
