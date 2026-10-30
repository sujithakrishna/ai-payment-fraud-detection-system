package com.sujitha.aipaymentfrauddetection.service;

import com.sujitha.aipaymentfrauddetection.entity.FraudRiskScore;
import com.sujitha.aipaymentfrauddetection.repository.FraudRiskScoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FraudRiskScoreService {

    private final FraudRiskScoreRepository fraudRiskScoreRepository;

    public FraudRiskScoreService(
            FraudRiskScoreRepository fraudRiskScoreRepository) {
        this.fraudRiskScoreRepository = fraudRiskScoreRepository;
    }

    public List<FraudRiskScore> getAllRiskScores() {
        return fraudRiskScoreRepository.findAllByOrderByRiskIdDesc();
    }

    public Optional<FraudRiskScore> getRiskScoreByTransactionId(
            Long transactionId) {

        return fraudRiskScoreRepository
                .findByTransactionId(transactionId);
    }
}