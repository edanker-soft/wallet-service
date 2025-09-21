package com.edanker.soft.walletservice.repository;

import com.edanker.soft.walletservice.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {
}
