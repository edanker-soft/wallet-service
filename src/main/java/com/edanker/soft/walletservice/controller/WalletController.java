package com.edanker.soft.walletservice.controller;

import com.edanker.soft.walletservice.controller.dto.BalanceResponseDTO;
import com.edanker.soft.walletservice.controller.dto.CreateWalletDTO;
import com.edanker.soft.walletservice.controller.dto.DepositDTO;
import com.edanker.soft.walletservice.controller.dto.HistoricalBalanceRequestDTO;
import com.edanker.soft.walletservice.controller.dto.TransferDTO;
import com.edanker.soft.walletservice.controller.dto.WithdrawDTO;
import com.edanker.soft.walletservice.entity.Transfer;
import com.edanker.soft.walletservice.entity.Wallet;
import com.edanker.soft.walletservice.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallets")
public class WalletController {

  private final WalletService walletService;

  public WalletController(WalletService walletService) {
    this.walletService = walletService;
  }

  // Create wallet
  @PostMapping
  public ResponseEntity<Wallet> createWallet(
      @RequestBody @Valid CreateWalletDTO dto) {
    var wallet = walletService.createWallet(dto);
    return ResponseEntity.ok(wallet);
  }

  // Get current balance
  @GetMapping("/{walletId}/balance")
  public ResponseEntity<BalanceResponseDTO> getCurrentBalance(@PathVariable Long walletId) {
    BigDecimal balance = walletService.getCurrentBalance(walletId);
    BalanceResponseDTO response = new BalanceResponseDTO(balance, LocalDateTime.now());
    return ResponseEntity.ok(response);
  }

  // Deposit funds
  @PostMapping("/{walletId}/deposit")
  public ResponseEntity<Void> deposit(@PathVariable Long walletId,
      @RequestBody @Valid DepositDTO depositDTO) {
    walletService.deposit(walletId, depositDTO);
    return ResponseEntity.ok().build();
  }

  // Withdraw funds
  @PostMapping("/{walletId}/withdraw")
  public ResponseEntity<Void> withdraw(@PathVariable Long walletId,
      @RequestBody @Valid WithdrawDTO withdrawDTO) {
    walletService.withdraw(walletId, withdrawDTO);
    return ResponseEntity.ok().build();
  }

  // Get historical balance
  @PostMapping("/{walletId}/historical-balance")
  public ResponseEntity<BalanceResponseDTO> getHistoricalBalance(@PathVariable Long walletId,
      @RequestBody @Valid HistoricalBalanceRequestDTO request) {
    BigDecimal balance = walletService.getHistoricalBalance(walletId, request.dateTime());
    BalanceResponseDTO response = new BalanceResponseDTO(balance, request.dateTime());
    return ResponseEntity.ok(response);
  }

  // Transfer funds
  @PostMapping("/transfer")
  public ResponseEntity<Transfer> transfer(@RequestBody @Valid TransferDTO dto) {
    var resp = walletService.transfer(dto);
    return ResponseEntity.ok(resp);
  }
}

