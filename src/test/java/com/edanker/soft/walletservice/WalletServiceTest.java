package com.edanker.soft.walletservice;

import com.edanker.soft.walletservice.controller.dto.CreateWalletDTO;
import com.edanker.soft.walletservice.controller.dto.DepositDTO;
import com.edanker.soft.walletservice.controller.dto.TransferDTO;
import com.edanker.soft.walletservice.controller.dto.TransferResponseDTO;
import com.edanker.soft.walletservice.controller.dto.WithdrawDTO;
import com.edanker.soft.walletservice.entity.Transfer;
import com.edanker.soft.walletservice.entity.Wallet;
import com.edanker.soft.walletservice.entity.Transaction;
import com.edanker.soft.walletservice.exceptions.InsufficientBalanceException;
import com.edanker.soft.walletservice.exceptions.WalletDataAlreadyExistsException;
import com.edanker.soft.walletservice.exceptions.WalletNotFoundException;
import com.edanker.soft.walletservice.repository.TransferRepository;
import com.edanker.soft.walletservice.repository.WalletRepository;
import com.edanker.soft.walletservice.repository.TransactionRepository;
import com.edanker.soft.walletservice.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

  @Mock
  private WalletRepository walletRepository;
  @Mock
  private TransactionRepository transactionRepository;
  @Mock
  private TransferRepository transferRepository;

  @InjectMocks
  private WalletService walletService;

  private Wallet testWallet1;
  private Wallet testWallet2;
  private CreateWalletDTO createWalletDTO;
  private DepositDTO depositDTO;
  private WithdrawDTO withdrawDTO;
  private TransferDTO transferDTO;

  @BeforeEach
  void setUp() {
    testWallet1 = Wallet.builder()
        .id(1L)
        .fullName("John Doe")
        .cpfCnpj("12345678901")
        .email("john.doe@example.com")
        .password("password123")
        .balance(new BigDecimal("100.00"))
        .build();

    testWallet2 = Wallet.builder()
        .id(2L)
        .fullName("Jane Smith")
        .cpfCnpj("09876543210")
        .email("jane.smith@example.com")
        .password("password456")
        .balance(new BigDecimal("50.00"))
        .build();

    createWalletDTO = CreateWalletDTO.builder()
        .fullName("New User")
        .cpfCnpj("11122233344")
        .email("new.user@example.com")
        .password("newpass")
        .build();

    depositDTO = DepositDTO.builder().amount(new BigDecimal("50.00")).build();
    withdrawDTO = WithdrawDTO.builder().amount(new BigDecimal("20.00")).build();
    transferDTO = TransferDTO.builder().payer(1L).payee(2L).value(new BigDecimal("30.00")).build();
  }

  @Test
  @DisplayName("Should create a new wallet successfully")
  void createWallet_Success() {
    when(walletRepository.findByCpfCnpjOrEmail(anyString(), anyString())).thenReturn(Optional.empty());
    when(walletRepository.save(any(Wallet.class))).thenReturn(createWalletDTO.toWallet());

    Wallet createdWallet = walletService.createWallet(createWalletDTO);

    assertNotNull(createdWallet);
    assertEquals(createWalletDTO.fullName(), createdWallet.getFullName());
    verify(walletRepository, times(1)).findByCpfCnpjOrEmail(anyString(), anyString());
    verify(walletRepository, times(1)).save(any(Wallet.class));
  }

  @Test
  @DisplayName("Should throw WalletDataAlreadyExistsException when creating wallet with existing CPF/CNPJ or email")
  void createWallet_WalletDataAlreadyExists() {
    when(walletRepository.findByCpfCnpjOrEmail(anyString(), anyString())).thenReturn(Optional.of(testWallet1));

    assertThrows(WalletDataAlreadyExistsException.class, () -> walletService.createWallet(createWalletDTO));
    verify(walletRepository, times(1)).findByCpfCnpjOrEmail(anyString(), anyString());
    verify(walletRepository, never()).save(any(Wallet.class));
  }

  @Test
  @DisplayName("Should get current balance successfully")
  void getCurrentBalance_Success() {
    when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet1));

    BigDecimal balance = walletService.getCurrentBalance(1L);

    assertEquals(new BigDecimal("100.00"), balance);
    verify(walletRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Should throw WalletNotFoundException when getting balance for non-existent wallet")
  void getCurrentBalance_WalletNotFound() {
    when(walletRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThrows(WalletNotFoundException.class, () -> walletService.getCurrentBalance(99L));
    verify(walletRepository, times(1)).findById(99L);
  }

  @Test
  @DisplayName("Should get historical balance successfully")
  void getHistoricalBalance_Success() {
    LocalDateTime historicalTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
    Transaction transaction1 = Transaction.builder()
        .wallet(testWallet1)
        .timestamp(historicalTime.minusHours(2))
        .balanceAfterOperation(new BigDecimal("50.00"))
        .build();
    Transaction transaction2 = Transaction.builder()
        .wallet(testWallet1)
        .timestamp(historicalTime.minusHours(1))
        .balanceAfterOperation(new BigDecimal("75.00"))
        .build();

    when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet1));
    when(transactionRepository.findByWalletIdAndTimestampLessThanEqualOrderByTimestampDesc(1L, historicalTime))
        .thenReturn(Arrays.asList(transaction2, transaction1)); // Order matters for historical balance logic

    BigDecimal historicalBalance = walletService.getHistoricalBalance(1L, historicalTime);

    assertEquals(new BigDecimal("75.00"), historicalBalance);
    verify(walletRepository, times(1)).findById(1L);
    verify(transactionRepository, times(1)).findByWalletIdAndTimestampLessThanEqualOrderByTimestampDesc(1L, historicalTime);
  }

  @Test
  @DisplayName("Should return zero for historical balance if no transactions before given time")
  void getHistoricalBalance_NoTransactionsBeforeTime() {
    LocalDateTime historicalTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
    when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet1));
    when(transactionRepository.findByWalletIdAndTimestampLessThanEqualOrderByTimestampDesc(1L, historicalTime))
        .thenReturn(Collections.emptyList());

    BigDecimal historicalBalance = walletService.getHistoricalBalance(1L, historicalTime);

    assertEquals(BigDecimal.ZERO, historicalBalance);
    verify(walletRepository, times(1)).findById(1L);
    verify(transactionRepository, times(1)).findByWalletIdAndTimestampLessThanEqualOrderByTimestampDesc(1L, historicalTime);
  }

  @Test
  @DisplayName("Should throw WalletNotFoundException when getting historical balance for non-existent wallet")
  void getHistoricalBalance_WalletNotFound() {
    when(walletRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThrows(WalletNotFoundException.class, () -> walletService.getHistoricalBalance(99L, LocalDateTime.now()));
    verify(walletRepository, times(1)).findById(99L);
    verify(transactionRepository, never()).findByWalletIdAndTimestampLessThanEqualOrderByTimestampDesc(anyLong(), any(LocalDateTime.class));
  }

  @Test
  @DisplayName("Should deposit funds successfully")
  void deposit_Success() {
    when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet1));
    when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet1); // Mock save call

    walletService.deposit(1L, depositDTO);

    assertEquals(new BigDecimal("150.00"), testWallet1.getBalance());
    verify(walletRepository, times(1)).findById(1L);
    verify(walletRepository, times(1)).save(testWallet1);
    assertEquals(1, testWallet1.getTransactions().size());
  }

  @Test
  @DisplayName("Should throw WalletNotFoundException when depositing to non-existent wallet")
  void deposit_WalletNotFound() {
    when(walletRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThrows(WalletNotFoundException.class, () -> walletService.deposit(99L, depositDTO));
    verify(walletRepository, times(1)).findById(99L);
    verify(walletRepository, never()).save(any(Wallet.class));
  }

  @Test
  @DisplayName("Should withdraw funds successfully")
  void withdraw_Success() {
    when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet1));
    when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet1); // Mock save call

    walletService.withdraw(1L, withdrawDTO);

    assertEquals(new BigDecimal("80.00"), testWallet1.getBalance());
    verify(walletRepository, times(1)).findById(1L);
    verify(walletRepository, times(1)).save(testWallet1);
    assertEquals(1, testWallet1.getTransactions().size());
  }

  @Test
  @DisplayName("Should throw InsufficientBalanceException when withdrawing more than current balance")
  void withdraw_InsufficientBalance() {
    WithdrawDTO largeWithdraw = WithdrawDTO.builder().amount(new BigDecimal("200.00")).build();
    when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet1));

    assertThrows(InsufficientBalanceException.class, () -> walletService.withdraw(1L, largeWithdraw));
    verify(walletRepository, times(1)).findById(1L);
    verify(walletRepository, never()).save(any(Wallet.class));
    assertTrue(testWallet1.getTransactions().isEmpty()); // No transaction should be added
  }

  @Test
  @DisplayName("Should throw WalletNotFoundException when withdrawing from non-existent wallet")
  void withdraw_WalletNotFound() {
    when(walletRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThrows(WalletNotFoundException.class, () -> walletService.withdraw(99L, withdrawDTO));
    verify(walletRepository, times(1)).findById(99L);
    verify(walletRepository, never()).save(any(Wallet.class));
  }

  @Test
  @DisplayName("Should transfer funds successfully")
  void transfer_Success() {
    when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet1));
    when(walletRepository.findById(2L)).thenReturn(Optional.of(testWallet2));
    when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet1, testWallet2);
    when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> {
      Transfer transfer = invocation.getArgument(0);
      transfer.setId(UUID.randomUUID());
      transfer.setCreatedAt(LocalDateTime.now());
      return transfer;
    });

    TransferResponseDTO response = walletService.transfer(transferDTO);

    assertEquals(0, new BigDecimal("70.00").compareTo(testWallet1.getBalance()));
    assertEquals(0, new BigDecimal("80.00").compareTo(testWallet2.getBalance()));

    assertEquals(1, testWallet1.getTransactions().size());
    assertEquals(1, testWallet2.getTransactions().size());

    assertNotNull(response);
    assertEquals(transferDTO.payer(), response.senderId());
    assertEquals(transferDTO.payee(), response.receiverId());
    assertEquals(0, transferDTO.value().compareTo(response.value()));
    assertNotNull(response.id());
    assertNotNull(response.createdAt());

    verify(walletRepository, times(1)).findById(1L);
    verify(walletRepository, times(1)).findById(2L);
    verify(walletRepository, times(2)).save(any(Wallet.class));
    verify(transferRepository, times(1)).save(any(Transfer.class));
  }

  @Test
  @DisplayName("Should throw WalletNotFoundException when sender wallet not found during transfer")
  void transfer_SenderWalletNotFound() {
    when(walletRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(WalletNotFoundException.class, () -> walletService.transfer(transferDTO));
    verify(walletRepository, times(1)).findById(1L);
    verify(walletRepository, never()).findById(2L);
    verify(walletRepository, never()).save(any(Wallet.class));
    verify(transferRepository, never()).save(any(Transfer.class));
  }

  @Test
  @DisplayName("Should throw WalletNotFoundException when receiver wallet not found during transfer")
  void transfer_ReceiverWalletNotFound() {
    when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet1));
    when(walletRepository.findById(2L)).thenReturn(Optional.empty());

    assertThrows(WalletNotFoundException.class, () -> walletService.transfer(transferDTO));
    verify(walletRepository, times(1)).findById(1L);
    verify(walletRepository, times(1)).findById(2L);
    verify(walletRepository, never()).save(any(Wallet.class));
    verify(transferRepository, never()).save(any(Transfer.class));
  }

  @Test
  @DisplayName("Should throw InsufficientBalanceException when sender has insufficient balance for transfer")
  void transfer_InsufficientBalance() {
    TransferDTO largeTransfer = TransferDTO.builder().payer(1L).payee(2L).value(new BigDecimal("200.00")).build();
    when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet1));
    when(walletRepository.findById(2L)).thenReturn(Optional.of(testWallet2));

    assertThrows(InsufficientBalanceException.class, () -> walletService.transfer(largeTransfer));
    verify(walletRepository, times(1)).findById(1L);
    verify(walletRepository, times(1)).findById(2L);
    verify(walletRepository, never()).save(any(Wallet.class));
    verify(transferRepository, never()).save(any(Transfer.class));
    assertTrue(testWallet1.getTransactions().isEmpty());
    assertTrue(testWallet2.getTransactions().isEmpty());
  }
}
