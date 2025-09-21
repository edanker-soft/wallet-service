package com.edanker.soft.walletservice.controller.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record BalanceResponseDTO(BigDecimal balance, LocalDateTime timestamp) {
}

