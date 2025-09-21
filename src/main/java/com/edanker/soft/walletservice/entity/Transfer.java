package com.edanker.soft.walletservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transfer")
public class Transfer {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "wallet_sender_id")
  private Wallet sender;

  @ManyToOne
  @JoinColumn(name = "wallet_receiver_id")
  private Wallet receiver;

  @Column(name = "value")
  private BigDecimal value;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  public Transfer() {
  }

  public Transfer(Wallet sender, Wallet receiver, BigDecimal value) {
    this.sender = sender;
    this.receiver = receiver;
    this.value = value;
  }

  // Getters and setters...
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Wallet getSender() {
    return sender;
  }

  public void setSender(Wallet sender) {
    this.sender = sender;
  }

  public Wallet getReceiver() {
    return receiver;
  }

  public void setReceiver(Wallet receiver) {
    this.receiver = receiver;
  }

  public BigDecimal getValue() {
    return value;
  }

  public void setValue(BigDecimal value) {
    this.value = value;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
