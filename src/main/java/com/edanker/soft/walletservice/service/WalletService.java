package com.edanker.soft.walletservice.service;

import com.edanker.soft.walletservice.controller.dto.CreateWalletDTO;
import com.edanker.soft.walletservice.controller.dto.DepositDTO;
import com.edanker.soft.walletservice.controller.dto.TransferDTO;
import com.edanker.soft.walletservice.controller.dto.WithdrawDTO;
import com.edanker.soft.walletservice.entity.Transaction;
import com.edanker.soft.walletservice.entity.Transfer;
import com.edanker.soft.walletservice.entity.Wallet;
import com.edanker.soft.walletservice.exceptions.InsufficientBalanceException;
import com.edanker.soft.walletservice.exceptions.WalletDataAlreadyExistsException;
import com.edanker.soft.walletservice.exceptions.WalletNotFoundException;
import com.edanker.soft.walletservice.repository.TransactionRepository;
import com.edanker.soft.walletservice.repository.TransferRepository;
import com.edanker.soft.walletservice.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WalletService {

  private final WalletRepository walletRepository;
  private final TransactionRepository transactionRepository;
  private final TransferRepository transferRepository;

  public WalletService(WalletRepository walletRepository,
      TransactionRepository transactionRepository,
      TransferRepository transferRepository) {
    this.walletRepository = walletRepository;
    this.transactionRepository = transactionRepository;
    this.transferRepository = transferRepository;
  }

  public Wallet createWallet(CreateWalletDTO dto) {
    var walletDb = walletRepository.findByCpfCnpjOrEmail(dto.cpfCnpj(), dto.email());
    if (walletDb.isPresent()) {
      throw new WalletDataAlreadyExistsException("CpfCnpj or Email already exists");
    }
    return walletRepository.save(dto.toWallet());
  }

  public BigDecimal getCurrentBalance(Long walletId) {
    var wallet = walletRepository.findById(walletId)
        .orElseThrow(() -> new WalletNotFoundException(walletId));
    return wallet.getBalance();
  }

  public BigDecimal getHistoricalBalance(Long walletId, LocalDateTime dateTime) {
    var wallet = walletRepository.findById(walletId)
        .orElseThrow(() -> new WalletNotFoundException(walletId));

    // Get all transactions up to the specified date time
    List<Transaction> transactions = transactionRepository
        .findByWalletIdAndTimestampLessThanEqualOrderByTimestampDesc(walletId, dateTime);

    if (transactions.isEmpty()) {
      // If no transactions exist before the specified time, return initial balance (zero)
      return BigDecimal.ZERO;
    }

    // Return the balance after the last transaction before the specified time
    return transactions.get(0).getBalanceAfterOperation();
  }

  @Transactional
  public void deposit(Long walletId, DepositDTO depositDTO) {
    var wallet = walletRepository.findById(walletId)
        .orElseThrow(() -> new WalletNotFoundException(walletId));

    wallet.credit(depositDTO.amount(), "Deposit operation");

    walletRepository.save(wallet);
  }

  @Transactional
  public void withdraw(Long walletId, WithdrawDTO withdrawDTO) {
    var wallet = walletRepository.findById(walletId)
        .orElseThrow(() -> new WalletNotFoundException(walletId));

    validateWithdrawal(wallet, withdrawDTO.amount());

    wallet.debit(withdrawDTO.amount(), "Withdrawal operation");

    walletRepository.save(wallet);
  }

  @Transactional
  public Transfer transfer(TransferDTO transferDTO) {
    var sender = walletRepository.findById(transferDTO.payer())
        .orElseThrow(() -> new WalletNotFoundException(transferDTO.payer()));

    var receiver = walletRepository.findById(transferDTO.payee())
        .orElseThrow(() -> new WalletNotFoundException(transferDTO.payee()));

    validateTransfer(sender, transferDTO.value());

    // Perform the transfer using audit trail methods
    sender.debit(transferDTO.value(), "Transfer to wallet ID: " + transferDTO.payee());
    receiver.credit(transferDTO.value(), "Transfer from wallet ID: " + transferDTO.payer());

    // Save updated wallets
    walletRepository.save(sender);
    walletRepository.save(receiver);

    // Create and save transfer record
    var transfer = new Transfer(sender, receiver, transferDTO.value());
    return transferRepository.save(transfer);
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
