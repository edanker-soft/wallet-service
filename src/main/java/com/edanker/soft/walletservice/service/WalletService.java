package com.edanker.soft.walletservice.service;

import com.edanker.soft.walletservice.controller.dto.CreateWalletDTO;
import com.edanker.soft.walletservice.entity.Wallet;
import com.edanker.soft.walletservice.exceptions.WalletDataAlreadyExistsException;
import com.edanker.soft.walletservice.repository.WalletRepository;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

  private final WalletRepository walletRepository;

  public WalletService(WalletRepository walletRepository) {
    this.walletRepository = walletRepository;
  }

  public Wallet createWallet(CreateWalletDTO dto) {

    var walletDb = walletRepository.findByCpfCnpjOrEmail(dto.cpfCnpj(), dto.email());
    if (walletDb.isPresent()) {
      throw new WalletDataAlreadyExistsException("CpfCnpj or Email already exists");
    }

    return walletRepository.save(dto.toWallet());
  }
}
