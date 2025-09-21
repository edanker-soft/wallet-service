package com.edanker.soft.walletservice.controller;

import com.edanker.soft.walletservice.exceptions.WalletException;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(WalletException.class)
  public ProblemDetail handleWalletException(WalletException e) {
    return e.toProblemDetail();
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

    var fieldErrors = e.getFieldErrors()
        .stream()
        .map(f -> InvalidParam.builder()
            .name(f.getField())
            .reason(f.getDefaultMessage())
            .build())
        .toList();

    var pb = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

    pb.setTitle("Your request parameters didn't validate.");
    pb.setProperty("invalid-params", fieldErrors);

    return pb;
  }

  @Builder
  private record InvalidParam(String name, String reason) {
  }
}
