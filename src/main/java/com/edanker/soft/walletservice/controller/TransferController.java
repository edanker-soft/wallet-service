package com.edanker.soft.walletservice.controller;

import com.edanker.soft.walletservice.controller.dto.TransferDTO;
import com.edanker.soft.walletservice.entity.Transfer;
import com.edanker.soft.walletservice.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransferController {

  private final TransferService transferService;

  public TransferController(TransferService transferService) {
    this.transferService = transferService;
  }

  @PostMapping("/transfer")
  public ResponseEntity<Transfer> transfer(@RequestBody @Valid TransferDTO dto) {

    var resp = transferService.transfer(dto);

    return ResponseEntity.ok(resp);
  }
}
