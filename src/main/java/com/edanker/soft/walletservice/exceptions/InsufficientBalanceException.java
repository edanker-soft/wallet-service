package com.edanker.soft.walletservice.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

@Getter
public class InsufficientBalanceException extends WalletException {

  @Override
  public ProblemDetail toProblemDetail() {
    var pb = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);

    pb.setTitle("Insufficient balance.");
    pb.setDetail("You cannot transfer a value bigger than your current balance.");

    return pb;
  }
}
