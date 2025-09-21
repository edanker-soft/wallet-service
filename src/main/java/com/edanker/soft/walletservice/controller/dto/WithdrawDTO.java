package com.edanker.soft.walletservice.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WithdrawDTO(@DecimalMin("0.01") @NotNull BigDecimal amount) {
}
