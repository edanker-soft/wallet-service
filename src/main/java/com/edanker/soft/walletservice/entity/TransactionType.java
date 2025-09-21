package com.edanker.soft.walletservice.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TransactionType {
  CREDIT,
  DEBIT
}
