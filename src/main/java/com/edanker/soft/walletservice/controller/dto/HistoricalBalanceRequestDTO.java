package com.edanker.soft.walletservice.controller.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record HistoricalBalanceRequestDTO(LocalDateTime dateTime) {
}
