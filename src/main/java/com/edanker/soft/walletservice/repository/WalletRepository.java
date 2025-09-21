package com.edanker.soft.walletservice.repository;

import com.edanker.soft.walletservice.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

  Optional<Wallet> findByCpfCnpjOrEmail(String cpfCnpj, String email);
}
