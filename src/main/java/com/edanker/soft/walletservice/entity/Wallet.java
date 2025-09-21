package com.edanker.soft.walletservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wallet")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Wallet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "full_name")
  private String fullName;

  @Column(name = "cpf_cnpj", unique = true)
  private String cpfCnpj;

  @Column(name = "email", unique = true)
  private String email;

  @Column(name = "password")
  private String password;

  @Builder.Default
  @Column(name = "balance")
  private BigDecimal balance = BigDecimal.ZERO;

  @Builder.Default
  @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Transaction> transactions = new ArrayList<>();

  public boolean isBalanceEqualOrGreaterThan(BigDecimal value) {
    return this.balance.compareTo(value) >= 0;
  }

  public void debit(BigDecimal value, String description) {
    this.balance = this.balance.subtract(value);
    addTransaction(TransactionType.DEBIT, value, description);
  }

  public void credit(BigDecimal value, String description) {
    this.balance = this.balance.add(value);
    addTransaction(TransactionType.CREDIT, value, description);
  }

  private void addTransaction(TransactionType type, BigDecimal amount, String description) {
    var transaction = Transaction.builder()
        .wallet(this)
        .type(type)
        .amount(amount)
        .description(description)
        .timestamp(LocalDateTime.now())
        .balanceAfterOperation(this.balance)
        .build();
    this.transactions.add(transaction);
  }
}
