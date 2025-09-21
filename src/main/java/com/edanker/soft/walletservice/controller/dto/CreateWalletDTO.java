package com.edanker.soft.walletservice.controller.dto;

import com.edanker.soft.walletservice.entity.Wallet;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CreateWalletDTO(@NotBlank String fullName,
                              @NotBlank String cpfCnpj,
                              @NotBlank String email,
                              @NotBlank String password) {

  public Wallet toWallet() {
    return Wallet.builder()
        .fullName(fullName)
        .cpfCnpj(cpfCnpj)
        .email(email)
        .password(password)
        .build();
  }
}

