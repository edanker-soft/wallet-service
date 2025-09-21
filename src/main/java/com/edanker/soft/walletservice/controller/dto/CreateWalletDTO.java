package com.edanker.soft.walletservice.controller.dto;

import com.edanker.soft.walletservice.entity.Wallet;
import jakarta.validation.constraints.NotBlank;

public record CreateWalletDTO(@NotBlank String fullName,
                              @NotBlank String cpfCnpj,
                              @NotBlank String email,
                              @NotBlank String password) {

  public Wallet toWallet() {
    return new Wallet(
        fullName,
        cpfCnpj,
        email,
        password
    );
  }
}
