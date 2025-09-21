package com.edanker.soft.walletservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class WalletException extends RuntimeException {

  public ProblemDetail toProblemDetail() {
    var pb = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);

    pb.setTitle("Wallet service internal server error");

    return pb;
  }
}
