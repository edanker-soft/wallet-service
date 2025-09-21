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

@Entity
@Table(name = "wallet")
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

  @Column(name = "balance")
  private BigDecimal balance = BigDecimal.ZERO;

  // One-to-many relationship for transaction history
  @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Transaction> transactions = new ArrayList<>();

  public Wallet() {
  }

  public Wallet(String fullName, String cpfCnpj, String email, String password) {
    this.fullName = fullName;
    this.cpfCnpj = cpfCnpj;
    this.email = email;
    this.password = password;
  }

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
    Transaction transaction = new Transaction();
    transaction.setWallet(this);
    transaction.setType(type);
    transaction.setAmount(amount);
    transaction.setDescription(description);
    transaction.setTimestamp(LocalDateTime.now());
    transaction.setBalanceAfterOperation(this.balance);
    this.transactions.add(transaction);
  }

  // Getters and setters...
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getCpfCnpj() {
    return cpfCnpj;
  }

  public void setCpfCnpj(String cpfCnpj) {
    this.cpfCnpj = cpfCnpj;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  public List<Transaction> getTransactions() {
    return transactions;
  }

  public void setTransactions(List<Transaction> transactions) {
    this.transactions = transactions;
  }
}
