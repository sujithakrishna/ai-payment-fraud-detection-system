
package com.sujitha.aipaymentfrauddetection.service;

import com.sujitha.aipaymentfrauddetection.dto.MlPredictionRequest;
import com.sujitha.aipaymentfrauddetection.dto.MlPredictionResponse;
import com.sujitha.aipaymentfrauddetection.entity.FraudRiskScore;
import com.sujitha.aipaymentfrauddetection.entity.Transaction;
import com.sujitha.aipaymentfrauddetection.repository.TransactionRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Service
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;
    private final MlPredictionService mlPredictionService;

    public FraudDetectionService(
            TransactionRepository transactionRepository,
            MlPredictionService mlPredictionService) {

        this.transactionRepository = transactionRepository;
        this.mlPredictionService = mlPredictionService;
    }

    public FraudRiskScore analyzeTransaction(
            Transaction transaction) {

        int riskScore = 0;

        StringBuilder riskReason = new StringBuilder();

        LocalDateTime transactionTime =
                transaction.getTransactionTime();

        // RULE 1: High transaction amount
        // Amount > 10,000 = +40

        if (transaction.getAmount() != null
                && transaction.getAmount().doubleValue() > 10000) {

            riskScore += 40;

            riskReason.append("High transaction amount; ");
        }

        // RULE 2: High transaction frequency
        // More than 3 previous transactions within 10 minutes = +30

        LocalDateTime frequencyFromTime =
                transactionTime.minusMinutes(10);

        long frequencyCount =
                transactionRepository.countPreviousTransactionsIn10Minutes(
                        transaction.getSenderId(),
                        frequencyFromTime,
                        transactionTime,
                        transaction.getTransactionId()
                );

        if (frequencyCount > 3) {

            riskScore += 30;

            riskReason.append(
                    "High transaction frequency: "
                            + frequencyCount
                            + " previous transactions in 10 minutes; "
            );
        }

        // RULE 3: Duplicate transaction
        // Same sender, receiver, amount and transaction type
        // within 5 minutes = +20

        LocalDateTime duplicateFromTime =
                transactionTime.minusMinutes(5);

        long duplicateCount =
                transactionRepository.countDuplicateTransactions(
                        transaction.getSenderId(),
                        transaction.getReceiverId(),
                        transaction.getAmount(),
                        transaction.getTransactionType(),
                        duplicateFromTime,
                        transactionTime,
                        transaction.getTransactionId()
                );

        if (duplicateCount > 0) {

            riskScore += 20;

            riskReason.append(
                    "Duplicate transaction: "
                            + duplicateCount
                            + " similar transaction(s) within 5 minutes; "
            );
        }

        // RULE 4: Different transaction location
        // Different location within 10 minutes = +30

        LocalDateTime locationFromTime =
                transactionTime.minusMinutes(10);

        long differentLocationCount =
                transactionRepository.countDifferentLocationTransactions(
                        transaction.getSenderId(),
                        transaction.getLocation(),
                        locationFromTime,
                        transactionTime,
                        transaction.getTransactionId()
                );

        if (differentLocationCount > 0) {

            riskScore += 30;

            riskReason.append(
                    "Transaction from different location: "
                            + differentLocationCount
                            + " previous transaction(s) from another location within 10 minutes; "
            );
        }

        // Determine Java rule-based risk level

        String riskLevel;

        if (riskScore < 30) {

            riskLevel = "LOW";

        } else if (riskScore < 70) {

            riskLevel = "MEDIUM";

        } else {

            riskLevel = "HIGH";
        }

        // Prepare ML prediction features

        long receiverTransactionCount =
                transactionRepository.countReceiverTransactionsIn10Minutes(
                        transaction.getReceiverId(),
                        frequencyFromTime,
                        transactionTime,
                        transaction.getTransactionId()
                );

        Optional<LocalDateTime> lastTransactionTime =
                transactionRepository.findLastTransactionTimeBySender(
                        transaction.getSenderId(),
                        transactionTime,
                        transaction.getTransactionId()
                );

        // Use 1440 minutes when no previous sender transaction exists.

        double minutesSinceLastTransaction = 1440.0;

        if (lastTransactionTime.isPresent()) {

            minutesSinceLastTransaction =
                    Duration.between(
                            lastTransactionTime.get(),
                            transactionTime
                    ).toSeconds() / 60.0;

            minutesSinceLastTransaction =
                    Math.max(0.1, minutesSinceLastTransaction);
        }

        int locationChanged =
                differentLocationCount > 0 ? 1 : 0;

        int transactionHour =
                transactionTime.getHour();

        // Call Python Random Forest prediction service

        String detectionMethod = "RULE_BASED";

        try {

            MlPredictionRequest mlRequest =
                    new MlPredictionRequest(
                            transaction.getAmount(),
                            transaction.getTransactionType(),
                            transaction.getPaymentMethod(),
                            (int) frequencyCount,
                            (int) receiverTransactionCount,
                            minutesSinceLastTransaction,
                            locationChanged,
                            transactionHour
                    );

            MlPredictionResponse mlResponse =
                    mlPredictionService.predict(mlRequest);

            if (mlResponse != null
                    && mlResponse.getPrediction() != null) {

                detectionMethod = "RULE_BASED+ML";

                double fraudProbability =
                        mlResponse.getFraudProbability();

                String probabilityPercentage =
                        String.format(
                                Locale.ROOT,
                                "%.2f",
                                fraudProbability * 100
                        );

                riskReason.append(
                        "ML prediction: "
                                + mlResponse.getPrediction()
                                + "; Fraud probability: "
                                + probabilityPercentage
                                + "% (synthetic-data model); "
                );
            }

        } catch (RestClientException exception) {

            // Python service unavailable.
            // Continue with existing Java rules.

            riskReason.append(
                    "ML service unavailable; rule-based detection used; "
            );
        }

        // No suspicious activity detected by Java rules

        if (riskScore == 0) {

            riskReason.append(
                    "No suspicious activity detected by Java rules"
            );
        }

        // Build FraudRiskScore object.
        // Saving is handled by TransactionService.

        FraudRiskScore fraudRiskScore =
                new FraudRiskScore();

        fraudRiskScore.setTransactionId(
                transaction.getTransactionId());

        fraudRiskScore.setRiskScore(
                BigDecimal.valueOf(riskScore));

        fraudRiskScore.setRiskLevel(
                riskLevel);

        fraudRiskScore.setRiskReason(
                riskReason.toString());

        fraudRiskScore.setDetectionMethod(
                detectionMethod);

        return fraudRiskScore;
    }
}
