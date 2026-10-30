package com.sujitha.aipaymentfrauddetection.repository;

import com.sujitha.aipaymentfrauddetection.entity.TransactionReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionReviewRepository
        extends JpaRepository<TransactionReview, Long> {

    Optional<TransactionReview> findByTransactionId(Long transactionId);

    List<TransactionReview> findByReviewStatusOrderByCreatedAtAsc(
            String reviewStatus
    );
}