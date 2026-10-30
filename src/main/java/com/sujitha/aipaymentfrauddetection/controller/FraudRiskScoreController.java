package com.sujitha.aipaymentfrauddetection.controller;

import com.sujitha.aipaymentfrauddetection.entity.FraudRiskScore;
import com.sujitha.aipaymentfrauddetection.service.FraudRiskScoreService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fraud-risk")
public class FraudRiskScoreController {

    private final FraudRiskScoreService fraudRiskScoreService;

    public FraudRiskScoreController(
            FraudRiskScoreService fraudRiskScoreService) {
        this.fraudRiskScoreService = fraudRiskScoreService;
    }

    @GetMapping
    public List<FraudRiskScore> getAllRiskScores() {
        return fraudRiskScoreService.getAllRiskScores();
    }

    @GetMapping("/transaction/{transactionId}")
    public FraudRiskScore getRiskScoreByTransaction(
            @PathVariable Long transactionId) {

        return fraudRiskScoreService
                .getRiskScoreByTransactionId(transactionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Fraud risk score not found for transaction: "
                                        + transactionId));
    }
}