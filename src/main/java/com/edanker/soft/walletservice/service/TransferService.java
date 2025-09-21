package com.edanker.soft.walletservice.service;

import com.edanker.soft.walletservice.controller.dto.TransferDTO;
import com.edanker.soft.walletservice.entity.Transfer;
import com.edanker.soft.walletservice.entity.Wallet;
import com.edanker.soft.walletservice.exceptions.InsufficientBalanceException;
import com.edanker.soft.walletservice.exceptions.WalletNotFoundException;
import com.edanker.soft.walletservice.repository.TransferRepository;
import com.edanker.soft.walletservice.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TransferService {

  private final TransferRepository transferRepository;
  private final WalletRepository walletRepository;

  public TransferService(TransferRepository transferRepository,
      WalletRepository walletRepository) {
    this.transferRepository = transferRepository;
    this.walletRepository = walletRepository;
  }

  @Transactional
  public Transfer transfer(TransferDTO transferDto) {

    var sender = walletRepository.findById(transferDto.payer())
        .orElseThrow(() -> new WalletNotFoundException(transferDto.payer()));

    var receiver = walletRepository.findById(transferDto.payee())
        .orElseThrow(() -> new WalletNotFoundException(transferDto.payee()));

    validateTransfer(transferDto, sender);

    sender.debit(transferDto.value());
    receiver.credit(transferDto.value());

    var transfer = new Transfer(sender, receiver, transferDto.value());

    walletRepository.save(sender);
    walletRepository.save(receiver);

    return transferRepository.save(transfer);
  }

  private void validateTransfer(TransferDTO transferDto, Wallet sender) {
    if (!sender.isBalancerEqualOrGreatherThan(transferDto.value())) {
      throw new InsufficientBalanceException();
    }
  }
}
