package com.edanker.soft.walletservice.service;

import com.edanker.soft.walletservice.controller.dto.CreateWalletDTO;
import com.edanker.soft.walletservice.controller.dto.DepositDTO;
import com.edanker.soft.walletservice.controller.dto.TransferDTO;
import com.edanker.soft.walletservice.controller.dto.TransferResponseDTO;
import com.edanker.soft.walletservice.controller.dto.WithdrawDTO;
import com.edanker.soft.walletservice.entity.Transaction;
import com.edanker.soft.walletservice.entity.Transfer;
import com.edanker.soft.walletservice.entity.Wallet;
import com.edanker.soft.walletservice.exceptions.InsufficientBalanceException;
import com.edanker.soft.walletservice.exceptions.WalletDataAlreadyExistsException;
import com.edanker.soft.walletservice.exceptions.WalletNotFoundException;
import com.edanker.soft.walletservice.repository.TransferRepository;
import com.edanker.soft.walletservice.repository.WalletRepository;
import com.edanker.soft.walletservice.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

  private final WalletRepository walletRepository;
  private final TransactionRepository transactionRepository;
  private final TransferRepository transferRepository;

  public Wallet createWallet(CreateWalletDTO dto) {
    var walletDb = walletRepository.findByCpfCnpjOrEmail(dto.cpfCnpj(), dto.email());
    if (walletDb.isPresent()) {
      throw new WalletDataAlreadyExistsException("CpfCnpj or Email already exists");
    }
    return walletRepository.save(dto.toWallet());
  }

  public BigDecimal getCurrentBalance(Long walletId) {
    var wallet = findWalletByIdAndValidateExists(walletId);
    return wallet.getBalance();
  }

  public BigDecimal getHistoricalBalance(Long walletId, LocalDateTime dateTime) {
    findWalletByIdAndValidateExists(walletId);

    List<Transaction> transactions = transactionRepository
        .findByWalletIdAndTimestampLessThanEqualOrderByTimestampDesc(walletId, dateTime);

    if (transactions.isEmpty()) {
      return BigDecimal.ZERO;
    }

    return transactions.getFirst().getBalanceAfterOperation();
  }

  @Transactional
  public void deposit(Long walletId, DepositDTO depositDTO) {
    var wallet = findWalletByIdAndValidateExists(walletId);

    wallet.credit(depositDTO.amount(), "Deposit operation");
    walletRepository.save(wallet);
  }

  @Transactional
  public void withdraw(Long walletId, WithdrawDTO withdrawDTO) {
    var wallet = findWalletByIdAndValidateExists(walletId);

    validateWithdrawal(wallet, withdrawDTO.amount());
    wallet.debit(withdrawDTO.amount(), "Withdrawal operation");
    walletRepository.save(wallet);
  }

  @Transactional
  public TransferResponseDTO transfer(TransferDTO transferDTO) {
    var sender = findWalletByIdAndValidateExists(transferDTO.payer());
    var receiver = findWalletByIdAndValidateExists(transferDTO.payee());

    validateTransfer(sender, transferDTO.value());

    sender.debit(transferDTO.value(), "Transfer to wallet ID: " + transferDTO.payee());
    receiver.credit(transferDTO.value(), "Transfer from wallet ID: " + transferDTO.payer());

    walletRepository.save(sender);
    walletRepository.save(receiver);

    var transfer = Transfer.builder()
        .sender(sender)
        .receiver(receiver)
        .value(transferDTO.value())
        .build();

    var savedTransfer = transferRepository.save(transfer);

    return TransferResponseDTO.builder()
        .id(savedTransfer.getId())
        .senderId(sender.getId())
        .senderName(sender.getFullName())
        .receiverId(receiver.getId())
        .receiverName(receiver.getFullName())
        .value(savedTransfer.getValue())
        .createdAt(savedTransfer.getCreatedAt())
        .build();
  }

  private Wallet findWalletByIdAndValidateExists(Long walletId) {
    return walletRepository.findById(walletId)
        .orElseThrow(() -> new WalletNotFoundException(walletId));
  }

  private static void validateWithdrawal(Wallet wallet, BigDecimal amount) {
    if (!wallet.isBalanceEqualOrGreaterThan(amount)) {
      throw new InsufficientBalanceException();
    }
  }

  private static void validateTransfer(Wallet sender, BigDecimal amount) {
    if (!sender.isBalanceEqualOrGreaterThan(amount)) {
      throw new InsufficientBalanceException();
    }
  }
}
