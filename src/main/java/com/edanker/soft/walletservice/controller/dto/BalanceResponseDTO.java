package com.edanker.soft.walletservice.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BalanceResponseDTO(BigDecimal balance, LocalDateTime timestamp) {
}

