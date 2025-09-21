package com.edanker.soft.walletservice.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DepositDTO(@DecimalMin("0.01") @NotNull BigDecimal amount) {
}
