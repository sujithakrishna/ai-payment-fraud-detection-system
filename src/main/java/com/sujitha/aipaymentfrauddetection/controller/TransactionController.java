package com.sujitha.aipaymentfrauddetection.controller;

import com.sujitha.aipaymentfrauddetection.dto.CreateTransactionRequest;
import com.sujitha.aipaymentfrauddetection.dto.TransactionResponse;
import com.sujitha.aipaymentfrauddetection.entity.Transaction;
import com.sujitha.aipaymentfrauddetection.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // ---------------------------------------------------------
    // Get all transactions
    // ---------------------------------------------------------

    @GetMapping
    public List<Transaction> getAllTransactions() {

        return transactionService.getAllTransactions();
    }

    // ---------------------------------------------------------
    // Get transaction by ID
    // ---------------------------------------------------------

    @GetMapping("/{transactionId}")
    public Transaction getTransactionById(
            @PathVariable Long transactionId) {

        return transactionService.getTransactionById(
                transactionId);
    }

    // ---------------------------------------------------------
    // Create transaction
    // ---------------------------------------------------------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {

        return transactionService.createTransaction(request);
    }
}