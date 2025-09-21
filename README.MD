# Wallet Service

A comprehensive wallet service API built with Java 21, Spring Boot, and MySQL that supports create wallet, balance queries, deposits, withdrawals, transfers, and historical balance lookups. This project provides RESTful API interfaces with proper error handling, audit trails, and Docker support.

## Table of Contents
- [Project Structure](#project-structure)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Installation & Running](#installation--running)
    - [Prerequisites](#prerequisites)
    - [Installation Steps](#installation-steps)
- [API Usage](#api-usage)
    - [Base Routes](#base-routes)
    - [Example Requests](#example-requests)
- [Database Schema](#database-schema)
- [Error Handling](#error-handling)
- [Audit Trail](#audit-trail)
- [License](#license)

## Project Structure

```plaintext
wallet-service/
├── src/main/java/com/edanker/soft/walletservice/
│   ├── controller/              # REST API controllers
│   │   └── WalletController.java # Main wallet operations controller
│   ├── service/                 # Business logic layer
│   │   └── WalletService.java   # Wallet service implementation
│   ├── entity/                  # JPA entities
│   │   ├── Wallet.java          # Wallet entity
│   │   ├── Transaction.java     # Transaction audit entity
│   │   ├── TransactionType.java # Transaction type enum
│   │   └── Transfer.java        # Transfer entity
│   ├── repository/              # Data access layer
│   │   ├── WalletRepository.java
│   │   ├── TransactionRepository.java
│   │   └── TransferRepository.java
│   ├── controller/dto/          # Data transfer objects
│   │   ├── CreateWalletDTO.java
│   │   ├── TransferDTO.java
│   │   ├── DepositDTO.java
│   │   ├── WithdrawDTO.java
│   │   ├── TransferResponseDTO.java
│   │   ├── BalanceResponseDTO.java
│   │   └── HistoricalBalanceRequestDTO.java
│   ├── exceptions/              # Custom exceptions
│   │   ├── WalletException.java
│   │   ├── InsufficientBalanceException.java
│   │   ├── WalletNotFoundException.java
│   │   └── WalletDataAlreadyExistsException.java
│   └── WalletServiceApplication.java # Application entry point
├── src/main/resources/
│   └── application.properties      # Configuration file
├── docker/
│   └── docker-compose.yml          # Docker Compose configuration
├── build.gradle                    # Gradle build file
└── README.md                       # Project README file
```

## Features

This wallet service implements the following core features:

- **Create Wallet**: Register new user wallets with unique identifiers
- **Deposit Functionality**: Users can deposit money into their wallets with full audit trail
- **Withdrawal Functionality**: Users can withdraw money from their wallets with balance validation
- **Transfer Functionality**: Users can transfer money between wallets with atomic operations
- **Current Balance Query**: Users can check their real-time wallet balance
- **Historical Balance Query**: Users can retrieve wallet balance at any specific point in time
- **Full Audit Trail**: All operations are logged for compliance and debugging
- **Comprehensive Error Handling**: Proper Problem Details RFC 7807 compliant error responses

## Tech Stack

- **Java 21**: Modern Java platform with latest features
- **Spring Boot**: Rapid application development framework
- **Spring Data JPA**: ORM and database abstraction
- **MySQL**: Relational database for persistent storage
- **Docker**: Containerization for easy deployment
- **Lombok**: Boilerplate code reduction
- **Hibernate Validator**: Request validation
- **ControllerAdvice & Problem Details**: Standardized error handling

## Installation & Running

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- Git

### Installation Steps

1. Clone the repository:

    ```bash
    git clone https://github.com/edanker-soft/wallet-service.git
    cd wallet-service
    ```

2. Start MySQL database using Docker Compose:

    ```bash
    docker-compose -f docker/docker-compose.yml up -d
    ```

   This will create a MySQL database with the following configuration:
    - Database: `wallet_db`
    - Username: `admin`
    - Password: `123`
    - Port: `3306`

3. Wait for MySQL to initialize (usually takes 10-15 seconds)

4. Build and start the service:

    ```bash
    ./gradlew bootRun
    ```

   Or build and run separately:
    ```bash
    ./gradlew clean build
    java -jar build/libs/wallet-service-*.jar
    ```

5. The service will be available at `http://localhost:8080`

## API Usage

### Base Routes

All routes are prefixed with `/wallets`. Here are the main API endpoints:

- `POST /wallets` - Create a new wallet
- `GET /wallets/{walletId}/balance` - Query current balance
- `GET /wallets/{walletId}/historical-balance?dateTime={timestamp}` - Query historical balance
- `POST /wallets/{walletId}/deposit` - Deposit funds
- `POST /wallets/{walletId}/withdraw` - Withdraw funds
- `POST /wallets/transfer` - Transfer funds between wallets

### Example Requests

**Create Wallet**
- Request: `POST http://localhost:8080/wallets`
    ```json
    {
        "fullName": "John Doe",
        "cpfCnpj": "12345678901",
        "email": "john.doe@example.com",
        "password": "securePassword123"
    }
    ```
- Response:
    ```json
    {
        "id": 1,
        "fullName": "John Doe",
        "cpfCnpj": "12345678901",
        "email": "john.doe@example.com",
        "balance": 0
    }
    ```

**Get Current Balance**
- Request: `GET http://localhost:8080/wallets/1/balance`
- Response:
    ```json
    {
        "balance": 100.50,
        "timestamp": "2024-01-15T14:30:45.123456"
    }
    ```

**Get Historical Balance**
- Request: `GET http://localhost:8080/wallets/1/historical-balance?dateTime=2024-01-15T10:00:00`
- Response:
    ```json
    {
        "balance": 75.25,
        "timestamp": "2024-01-15T10:00:00"
    }
    ```

**Deposit Funds**
- Request: `POST http://localhost:8080/wallets/1/deposit`
    ```json
    {
        "amount": 100.50
    }
    ```
- Response: `200 OK` (no body)

**Withdraw Funds**
- Request: `POST http://localhost:8080/wallets/1/withdraw`
    ```json
    {
        "amount": 25.75
    }
    ```
- Response: `200 OK` (no body)

**Transfer Funds**
- Request: `POST http://localhost:8080/wallets/transfer`
    ```json
    {
        "value": 50.00,
        "payer": 1,
        "payee": 2
    }
    ```
- Response:
    ```json
    {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "senderId": 1,
        "senderName": "John Doe",
        "receiverId": 2,
        "receiverName": "Jane Smith",
        "value": 50.00,
        "createdAt": "2024-01-15T14:30:45.123456"
    }
    ```

## Database Schema

The application automatically creates and updates the database schema. The main tables are:

1. **wallet** - Stores wallet information
2. **transaction** - Audit trail of all wallet operations
3. **transfer** - Records of inter-wallet transfers

Key relationships:
- Each wallet has many transactions (one-to-many)
- Transfers reference sender and receiver wallets
- All monetary operations create transaction records for audit purposes

## Error Handling

The service implements RFC 7807 Problem Details for HTTP APIs standard. Common error responses include:

**Insufficient Balance**
```json
{
    "type": "about:blank",
    "title": "Insufficient balance.",
    "status": 422,
    "detail": "You cannot transfer a value bigger than your current balance.",
    "instance": "/wallets/transfer"
}
```

**Validation Errors**
```json
{
    "type": "about:blank",
    "title": "Your request parameters didn't validate.",
    "status": 400,
    " invalid-params": [
        {
            "name": "amount",
            "reason": "must be greater than or equal to 0.01"
        }
    ]
}
```

**Wallet Not Found**
```json
{
    "type": "about:blank",
    "title": "Wallet not found",
    "status": 422,
    "detail": "There is no wallet with id 999.",
    "instance": "/wallets/999/balance"
}
```

## Testing

The project includes a comprehensive suite of unit tests for the service and controller layers to ensure code quality and correctness.

- **Frameworks**: Tests are written using JUnit 5 and Mockito.
- **Service Tests**: WalletServiceTest focuses on the business logic in isolation, with repository dependencies mocked. It covers all service methods, including success paths and exceptional cases like insufficient balance or wallet not found.
- **Controller Tests**: WalletControllerTest uses MockMvc with a standalone setup to test the web layer without loading a full Spring context. It verifies HTTP status codes, request/response serialization, and that controller-level exceptions are handled correctly by the RestExceptionHandler.

## Running Tests
 To run all unit tests, use the following Gradle command:

```bash
./gradlew test
```

A test report will be generated in build/reports/tests/test/index.html.

## Audit Trail

Every monetary operation is fully audited:

1. **Complete Transaction History**: All deposits, withdrawals, and transfers create immutable records
2. **Balance Tracking**: Each transaction stores the resulting balance
3. **Temporal Queries**: Historical balance lookups use transaction timestamps
4. **Operational Metadata**: Timestamps, operation types, and descriptions are recorded
5. **Mission-Critical Reliability**: Fully ACID-compliant operations ensure data integrity

This audit trail enables:
- Financial compliance reporting
- Balance reconciliation
- Debugging and troubleshooting
- Fraud detection and prevention
