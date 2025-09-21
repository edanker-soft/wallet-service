package com.edanker.soft.walletservice.controller.dto;

import lombok.Builder;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Builder
public record TransferDTO(@DecimalMin("0.01") @NotNull BigDecimal value,
                          @NotNull Long payer,
                          @NotNull Long payee) {
}
