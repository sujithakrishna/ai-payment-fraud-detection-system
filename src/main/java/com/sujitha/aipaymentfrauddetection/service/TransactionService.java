package com.sujitha.aipaymentfrauddetection.service;

import com.sujitha.aipaymentfrauddetection.dto.CreateTransactionRequest;
import com.sujitha.aipaymentfrauddetection.dto.TransactionResponse;
import com.sujitha.aipaymentfrauddetection.entity.FraudRiskScore;
import com.sujitha.aipaymentfrauddetection.entity.Transaction;
import com.sujitha.aipaymentfrauddetection.entity.TransactionReview;
import com.sujitha.aipaymentfrauddetection.repository.FraudRiskScoreRepository;
import com.sujitha.aipaymentfrauddetection.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final FraudRiskScoreRepository fraudRiskScoreRepository;
    private final FraudDetectionService fraudDetectionService;
    private final ReviewService reviewService;

    public TransactionService(
            TransactionRepository transactionRepository,
            FraudRiskScoreRepository fraudRiskScoreRepository,
            FraudDetectionService fraudDetectionService,
            ReviewService reviewService) {

        this.transactionRepository = transactionRepository;
        this.fraudRiskScoreRepository = fraudRiskScoreRepository;
        this.fraudDetectionService = fraudDetectionService;
        this.reviewService = reviewService;
    }

    // ---------------------------------------------------------
    // Get all transactions
    // ---------------------------------------------------------

    public List<Transaction> getAllTransactions() {

        return transactionRepository.findAll();
    }

    // ---------------------------------------------------------
    // Get transaction by ID
    // ---------------------------------------------------------

    public Transaction getTransactionById(Long transactionId) {

        return transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Transaction not found: "
                                        + transactionId));
    }

    // ---------------------------------------------------------
    // Create transaction
    // ---------------------------------------------------------

    @Transactional
    public TransactionResponse createTransaction(
            CreateTransactionRequest request) {

        // -----------------------------------------------------
        // 1. Create transaction
        // -----------------------------------------------------

        Transaction transaction = new Transaction();

        transaction.setSenderId(request.getSenderId());
        transaction.setReceiverId(request.getReceiverId());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setLocation(request.getLocation());

        // Transaction is initially pending
        transaction.setTransactionStatus("PENDING");

        transaction.setTransactionTime(LocalDateTime.now());

        transaction = transactionRepository.save(transaction);

        // -----------------------------------------------------
        // 2. Run fraud detection
        // -----------------------------------------------------

        FraudRiskScore fraudRiskScore =
                fraudDetectionService.analyzeTransaction(
                        transaction);

        // -----------------------------------------------------
        // 3. Save fraud risk score
        // -----------------------------------------------------

        fraudRiskScore =
                fraudRiskScoreRepository.save(
                        fraudRiskScore);

        // -----------------------------------------------------
        // 4. Determine transaction status
        // -----------------------------------------------------

        if ("LOW".equals(fraudRiskScore.getRiskLevel())) {

            transaction.setTransactionStatus("SUCCESS");

        } else {

            transaction.setTransactionStatus(
                    "PENDING_REVIEW");
        }

        transaction = transactionRepository.save(transaction);

        // -----------------------------------------------------
        // 5. Automatically create review for MEDIUM/HIGH
        // -----------------------------------------------------

        TransactionReview review = null;

        if (!"LOW".equals(
                fraudRiskScore.getRiskLevel())) {

            review = new TransactionReview();

            review.setTransactionId(
                    transaction.getTransactionId());

            // Sender is the user who initiated
            // the transaction
            review.setUserId(
                    transaction.getSenderId());

            review.setRiskScore(
                    fraudRiskScore.getRiskScore());

            review.setRiskLevel(
                    fraudRiskScore.getRiskLevel());

            review.setRiskReason(
                    fraudRiskScore.getRiskReason());

            review.setReviewStatus("PENDING");

            review.setCreatedAt(
                    LocalDateTime.now());

            review = reviewService.createReview(review);
        }

        // -----------------------------------------------------
        // 6. Build response
        // -----------------------------------------------------

        return buildTransactionResponse(
                transaction,
                fraudRiskScore,
                review);
    }

    // ---------------------------------------------------------
    // Build transaction response
    // ---------------------------------------------------------

    private TransactionResponse buildTransactionResponse(
            Transaction transaction,
            FraudRiskScore fraudRiskScore,
            TransactionReview review) {

        TransactionResponse response =
                new TransactionResponse();

        response.setTransactionId(
                transaction.getTransactionId());

        response.setSenderId(
                transaction.getSenderId());

        response.setReceiverId(
                transaction.getReceiverId());

        response.setAmount(
                transaction.getAmount());

        response.setTransactionType(
                transaction.getTransactionType());

        response.setTransactionStatus(
                transaction.getTransactionStatus());

        response.setPaymentMethod(
                transaction.getPaymentMethod());

        response.setLocation(
                transaction.getLocation());

        response.setTransactionTime(
                transaction.getTransactionTime());

        response.setRiskScore(
                fraudRiskScore.getRiskScore());

        response.setRiskLevel(
                fraudRiskScore.getRiskLevel());

        response.setRiskReason(
                fraudRiskScore.getRiskReason());

        response.setDetectionMethod(
                fraudRiskScore.getDetectionMethod());

        return response;
    }
}