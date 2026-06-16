# Midas Core — JP Morgan Chase Forage Project

A Spring Boot microservice built as part of the **JP Morgan Chase Advanced Software Engineering** . The service simulates a core banking transaction processing system that listens to a Kafka stream, validates transactions, applies incentives, and persists records to a database.

---

## What This Project Does

Midas Core acts as the backend engine for a financial platform called **Midas**. It:

- Consumes real-time financial transactions from an Apache Kafka topic
- Validates each transaction (checks sender existence, recipient existence, and sufficient balance)
- Calls an external Incentive API to fetch bonus amounts for eligible transactions
- Updates sender and recipient balances in the database
- Persists every processed transaction as a permanent record
- Exposes a REST endpoint to query any user's current balance

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Messaging | Apache Kafka + Spring Kafka |
| Persistence | Spring Data JPA + H2 (in-memory) |
| HTTP Client | RestTemplate |
| Testing | JUnit 5, Spring Boot Test, Embedded Kafka, Testcontainers |
| Build Tool | Maven (Maven Wrapper included) |

---

## Architecture

```
Kafka Topic (trader-updates)
        |
        v
 TransactionListener        <-- Kafka consumer
        |
        |-- Validates sender & recipient exist
        |-- Checks sender has sufficient balance
        |-- Calls Incentive API (localhost:8080/incentive)
        |-- Updates balances in DB
        |-- Saves TransactionRecord to DB
        |
        v
    H2 Database (UserRecord, TransactionRecord)

REST API:
  GET /balance?userId={id}   <-- BalanceController
```

The external **Incentive API** (`services/transaction-incentive-api.jar`) is a provided service that awards bonus amounts on top of certain transactions.

---

## Project Tasks Completed

This project was built incrementally across five tasks:

| Task | What Was Built |
|---|---|
| Task 1 | Set up the Spring Boot project and Kafka consumer |
| Task 2 | Connected to H2 database and seeded user data |
| Task 3 | Implemented transaction validation logic |
| Task 4 | Integrated the external Incentive API |
| Task 5 | Persisted TransactionRecords and exposed the `/balance` endpoint |

---

## How to Run

### Prerequisites

- Java 17+
- Maven (or use the included `mvnw` wrapper)
- The Incentive API service JAR (`services/transaction-incentive-api.jar`)

### Steps

**1. Start the Incentive API service**

```bash
java -jar services/transaction-incentive-api.jar
```

This starts the incentive service on port `8080`.

**2. Run the Spring Boot application**

```bash
./mvnw spring-boot:run
```

The Midas Core app starts on port `33400`.

**3. Run the tests**

```bash
./mvnw test
```

Tests use an embedded Kafka broker (no external Kafka setup required).

---

## Key Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/balance?userId={id}` | Returns current balance for a user |

---

## Project Structure

```
src/
 main/java/com/jpmc/midascore/
  ├── MidasCoreApplication.java       # App entry point
  ├── AppConfig.java                  # Bean configuration (RestTemplate)
  ├── TransactionListener.java        # Kafka consumer + transaction logic
  ├── component/
  │   └── BalanceController.java      # REST endpoint for balance queries
  ├── entity/
  │   ├── UserRecord.java             # JPA entity for users
  │   └── TransactionRecord.java      # JPA entity for transactions
  ├── foundation/
  │   ├── Transaction.java            # Kafka message model
  │   ├── Balance.java                # API response model
  │   └── Incentive.java              # Incentive API response model
  └── repository/
      ├── UserRepository.java         # Spring Data JPA repo for users
      └── TransactionRecordRepository.java  # Spring Data JPA repo for transactions

services/
  └── transaction-incentive-api.jar   # Provided external incentive service
```

---


