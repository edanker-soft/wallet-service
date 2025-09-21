package com.edanker.soft.walletservice.controller;

import com.edanker.soft.walletservice.controller.dto.BalanceResponseDTO;
import com.edanker.soft.walletservice.controller.dto.CreateWalletDTO;
import com.edanker.soft.walletservice.controller.dto.DepositDTO;
import com.edanker.soft.walletservice.controller.dto.HistoricalBalanceRequestDTO;
import com.edanker.soft.walletservice.controller.dto.TransferDTO;
import com.edanker.soft.walletservice.controller.dto.TransferResponseDTO;
import com.edanker.soft.walletservice.controller.dto.WithdrawDTO;
import com.edanker.soft.walletservice.entity.Transfer;
import com.edanker.soft.walletservice.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

  private final WalletService walletService;

  @PostMapping
  public ResponseEntity<com.edanker.soft.walletservice.entity.Wallet> createWallet(
      @RequestBody @Valid CreateWalletDTO dto) {
    var wallet = walletService.createWallet(dto);
    return ResponseEntity.ok(wallet);
  }

  @GetMapping("/{walletId}/balance")
  public ResponseEntity<BalanceResponseDTO> getCurrentBalance(@PathVariable Long walletId) {
    BigDecimal balance = walletService.getCurrentBalance(walletId);
    var response = BalanceResponseDTO.builder()
        .balance(balance)
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{walletId}/historical-balance")
  public ResponseEntity<BalanceResponseDTO> getHistoricalBalance(
      @PathVariable Long walletId,
      @RequestParam String dateTime) {

    var localDateTime = LocalDateTime.parse(dateTime);
    BigDecimal balance = walletService.getHistoricalBalance(walletId, localDateTime);
    var response = BalanceResponseDTO.builder()
        .balance(balance)
        .timestamp(localDateTime)
        .build();
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{walletId}/deposit")
  public ResponseEntity<Void> deposit(@PathVariable Long walletId,
      @RequestBody @Valid DepositDTO depositDTO) {
    walletService.deposit(walletId, depositDTO);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{walletId}/withdraw")
  public ResponseEntity<Void> withdraw(@PathVariable Long walletId,
      @RequestBody @Valid WithdrawDTO withdrawDTO) {
    walletService.withdraw(walletId, withdrawDTO);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/transfer")
  public ResponseEntity<TransferResponseDTO> transfer(@RequestBody @Valid TransferDTO dto) {
    var resp = walletService.transfer(dto);
    return ResponseEntity.ok(resp);
  }
}
