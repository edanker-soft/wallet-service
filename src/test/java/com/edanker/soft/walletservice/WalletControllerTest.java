package com.edanker.soft.walletservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edanker.soft.walletservice.controller.RestExceptionHandler;
import com.edanker.soft.walletservice.controller.WalletController;
import com.edanker.soft.walletservice.controller.dto.CreateWalletDTO;
import com.edanker.soft.walletservice.controller.dto.DepositDTO;
import com.edanker.soft.walletservice.controller.dto.TransferDTO;
import com.edanker.soft.walletservice.controller.dto.TransferResponseDTO;
import com.edanker.soft.walletservice.controller.dto.WithdrawDTO;
import com.edanker.soft.walletservice.entity.Wallet;
import com.edanker.soft.walletservice.exceptions.InsufficientBalanceException;
import com.edanker.soft.walletservice.exceptions.WalletDataAlreadyExistsException;
import com.edanker.soft.walletservice.exceptions.WalletNotFoundException;
import com.edanker.soft.walletservice.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Mock
  private WalletService walletService;

  @InjectMocks
  private WalletController walletController;

  private Wallet testWallet;
  private CreateWalletDTO createWalletDTO;
  private DepositDTO depositDTO;
  private WithdrawDTO withdrawDTO;
  private TransferDTO transferDTO;
  private TransferResponseDTO transferResponseDTO;

  @BeforeEach
  void setUp() {
    // Build a MockMvc instance for our controller, and register our custom exception handler
    mockMvc = MockMvcBuilders.standaloneSetup(walletController)
        .setControllerAdvice(
            new RestExceptionHandler())
        .build();

    // Initialize test data
    testWallet = Wallet.builder()
        .id(1L)
        .fullName("John Doe")
        .cpfCnpj("12345678901")
        .email("john.doe@example.com")
        .balance(new BigDecimal("100.00"))
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

    transferResponseDTO = TransferResponseDTO.builder()
        .id(UUID.randomUUID())
        .senderId(1L)
        .senderName("John Doe")
        .receiverId(2L)
        .receiverName("Jane Smith")
        .value(new BigDecimal("30.00"))
        .createdAt(LocalDateTime.now())
        .build();
  }

  @Test
  @DisplayName("POST /wallets - Should create a wallet successfully")
  void createWallet_Success() throws Exception {
    when(walletService.createWallet(any(CreateWalletDTO.class))).thenReturn(testWallet);

    mockMvc.perform(post("/wallets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createWalletDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(testWallet.getId()));
  }

  @Test
  @DisplayName("POST /wallets - Should return 422 for existing wallet data")
  void createWallet_WalletDataAlreadyExists() throws Exception {
    when(walletService.createWallet(any(CreateWalletDTO.class)))
        .thenThrow(new WalletDataAlreadyExistsException("CpfCnpj or Email already exists"));

    mockMvc.perform(post("/wallets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createWalletDTO)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.title").value("Wallet data already exists"));
  }

  @Test
  @DisplayName("GET /wallets/{walletId}/balance - Should get current balance successfully")
  void getCurrentBalance_Success() throws Exception {
    when(walletService.getCurrentBalance(1L)).thenReturn(new BigDecimal("100.00"));

    mockMvc.perform(get("/wallets/{walletId}/balance", 1L)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.balance").value(100.00));
  }

  @Test
  @DisplayName("POST /wallets/transfer - Should return 422 for insufficient balance")
  void transfer_InsufficientBalance() throws Exception {
    when(walletService.transfer(any(TransferDTO.class))).thenThrow(
        new InsufficientBalanceException());

    mockMvc.perform(post("/wallets/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(transferDTO)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.title").value("Insufficient balance."));
  }

  @Test
  @DisplayName("POST /wallets - Should return 400 for invalid CreateWalletDTO")
  void createWallet_InvalidDTO() throws Exception {
    CreateWalletDTO invalidDTO = CreateWalletDTO.builder()
        .fullName("")
        .cpfCnpj("11122233344")
        .email("new.user@example.com")
        .password("newpass")
        .build();

    mockMvc.perform(post("/wallets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Your request parameters didn't validate."))
        .andExpect(jsonPath("$.invalid-params[0].name").value("fullName"));
  }

  @Test
  @DisplayName("GET /wallets/{walletId}/balance - Should return 422 for non-existent wallet")
  void getCurrentBalance_WalletNotFound() throws Exception {
    when(walletService.getCurrentBalance(anyLong())).thenThrow(new WalletNotFoundException(99L));

    mockMvc.perform(get("/wallets/{walletId}/balance", 99L)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.title").value("Wallet not found"));
  }

  @Test
  @DisplayName("POST /wallets/{walletId}/deposit - Should deposit funds successfully")
  void deposit_Success() throws Exception {
    doNothing().when(walletService).deposit(anyLong(), any(DepositDTO.class));

    mockMvc.perform(post("/wallets/{walletId}/deposit", 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(depositDTO)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("POST /wallets/{walletId}/withdraw - Should withdraw funds successfully")
  void withdraw_Success() throws Exception {
    doNothing().when(walletService).withdraw(anyLong(), any(WithdrawDTO.class));

    mockMvc.perform(post("/wallets/{walletId}/withdraw", 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(withdrawDTO)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("POST /wallets/{walletId}/withdraw - Should return 422 for insufficient balance")
  void withdraw_InsufficientBalance() throws Exception {
    doThrow(new InsufficientBalanceException()).when(walletService).withdraw(anyLong(), any(WithdrawDTO.class));

    mockMvc.perform(post("/wallets/{walletId}/withdraw", 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(withdrawDTO)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.title").value("Insufficient balance."));
  }

  @Test
  @DisplayName("GET /wallets/{walletId}/historical-balance - Should get historical balance successfully")
  void getHistoricalBalance_Success() throws Exception {
    LocalDateTime historicalTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
    BigDecimal historicalBalance = new BigDecimal("75.00");

    when(walletService.getHistoricalBalance(anyLong(), any(LocalDateTime.class))).thenReturn(historicalBalance);

    mockMvc.perform(get("/wallets/{walletId}/historical-balance", 1L)
            .param("dateTime", historicalTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.balance").value(75.0))
        .andExpect(jsonPath("$.timestamp[0]").value(2024))
        .andExpect(jsonPath("$.timestamp[1]").value(1))
        .andExpect(jsonPath("$.timestamp[2]").value(15))
        .andExpect(jsonPath("$.timestamp[3]").value(10))
        .andExpect(jsonPath("$.timestamp[4]").value(0));
  }

  @Test
  @DisplayName("POST /wallets/transfer - Should transfer funds successfully")
  void transfer_Success() throws Exception {
    when(walletService.transfer(any(TransferDTO.class))).thenReturn(transferResponseDTO);

    mockMvc.perform(post("/wallets/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(transferDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.senderId").value(transferResponseDTO.senderId()))
        .andExpect(jsonPath("$.receiverId").value(transferResponseDTO.receiverId()))
        .andExpect(jsonPath("$.value").value(30.0));
  }

}
