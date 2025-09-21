package com.edanker.soft.walletservice.controller.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record TransferResponseDTO(
    UUID id,
    Long senderId,
    String senderName,
    Long receiverId,
    String receiverName,
    BigDecimal value,
    LocalDateTime createdAt
) {
}
