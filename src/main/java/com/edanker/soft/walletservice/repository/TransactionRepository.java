package com.edanker.soft.walletservice.repository;

import com.edanker.soft.walletservice.entity.Transaction;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  @Query("SELECT t FROM Transaction t "
      + "WHERE t.wallet.id = :walletId AND t.timestamp <= :dateTime ORDER BY t.timestamp DESC")
  List<Transaction> findByWalletIdAndTimestampLessThanEqualOrderByTimestampDesc(
      @Param("walletId") Long walletId, @Param("dateTime") LocalDateTime dateTime);
}
