package com.sujitha.aipaymentfrauddetection.repository;

import com.sujitha.aipaymentfrauddetection.entity.FraudRiskScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FraudRiskScoreRepository
        extends JpaRepository<FraudRiskScore, Long> {

    Optional<FraudRiskScore> findByTransactionId(Long transactionId);

    List<FraudRiskScore> findAllByOrderByRiskIdDesc();
}