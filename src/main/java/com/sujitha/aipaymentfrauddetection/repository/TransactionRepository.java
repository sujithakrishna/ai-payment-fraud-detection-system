
package com.sujitha.aipaymentfrauddetection.repository;

import com.sujitha.aipaymentfrauddetection.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionIdAndSenderId(
            Long transactionId,
            Integer senderId);

    @Query(value = """
            SELECT COUNT(*)
            FROM transactions
            WHERE sender_id = :senderId
              AND transaction_time >= :fromTime
              AND transaction_time < :transactionTime
              AND transaction_id <> :transactionId
            """, nativeQuery = true)
    long countPreviousTransactionsIn10Minutes(
            @Param("senderId") Integer senderId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("transactionTime") LocalDateTime transactionTime,
            @Param("transactionId") Long transactionId);

    @Query(value = """
            SELECT COUNT(*)
            FROM transactions
            WHERE sender_id = :senderId
              AND receiver_id = :receiverId
              AND amount = :amount
              AND transaction_type = :transactionType
              AND transaction_time >= :fromTime
              AND transaction_time < :transactionTime
              AND transaction_id <> :transactionId
            """, nativeQuery = true)
    long countDuplicateTransactions(
            @Param("senderId") Integer senderId,
            @Param("receiverId") Integer receiverId,
            @Param("amount") java.math.BigDecimal amount,
            @Param("transactionType") String transactionType,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("transactionTime") LocalDateTime transactionTime,
            @Param("transactionId") Long transactionId);

    @Query(value = """
            SELECT COUNT(*)
            FROM transactions
            WHERE sender_id = :senderId
              AND location <> :location
              AND transaction_time >= :fromTime
              AND transaction_time < :transactionTime
              AND transaction_id <> :transactionId
            """, nativeQuery = true)
    long countDifferentLocationTransactions(
            @Param("senderId") Integer senderId,
            @Param("location") String location,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("transactionTime") LocalDateTime transactionTime,
            @Param("transactionId") Long transactionId);

    // ML FEATURE 1: Receiver transaction frequency in 10 minutes
    @Query(value = """
            SELECT COUNT(*)
            FROM transactions
            WHERE receiver_id = :receiverId
              AND transaction_time >= :fromTime
              AND transaction_time < :transactionTime
              AND transaction_id <> :transactionId
            """, nativeQuery = true)
    long countReceiverTransactionsIn10Minutes(
            @Param("receiverId") Integer receiverId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("transactionTime") LocalDateTime transactionTime,
            @Param("transactionId") Long transactionId);

    // ML FEATURE 2: Most recent previous transaction of the sender
    @Query(value = """
            SELECT MAX(transaction_time)
            FROM transactions
            WHERE sender_id = :senderId
              AND transaction_time < :transactionTime
              AND transaction_id <> :transactionId
            """, nativeQuery = true)
    Optional<LocalDateTime> findLastTransactionTimeBySender(
            @Param("senderId") Integer senderId,
            @Param("transactionTime") LocalDateTime transactionTime,
            @Param("transactionId") Long transactionId);
}
