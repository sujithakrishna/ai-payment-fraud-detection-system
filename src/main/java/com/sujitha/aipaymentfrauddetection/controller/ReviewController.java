package com.sujitha.aipaymentfrauddetection.controller;

import com.sujitha.aipaymentfrauddetection.dto.ReviewCommentRequest;
import com.sujitha.aipaymentfrauddetection.dto.TransactionReviewResponse;
import com.sujitha.aipaymentfrauddetection.service.ReviewService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // ---------------------------------------------------------
    // Get review by transaction
    // ---------------------------------------------------------

    @GetMapping("/transaction/{transactionId}")
    public TransactionReviewResponse getReviewByTransactionId(
            @PathVariable Long transactionId) {

        return reviewService.getReviewByTransactionId(
                transactionId);
    }

    // ---------------------------------------------------------
    // Get pending reviews
    // ---------------------------------------------------------

    @GetMapping("/pending")
    public List<TransactionReviewResponse> getPendingReviews() {

        return reviewService.getPendingReviews();
    }

    // ---------------------------------------------------------
    // User submits explanation
    // ---------------------------------------------------------

    @PostMapping("/{reviewId}/description")
    public TransactionReviewResponse addUserDescription(
            @PathVariable Long reviewId,
            @RequestBody ReviewCommentRequest request) {

        return reviewService.addUserDescription(
                reviewId,
                request.getComment());
    }

    // ---------------------------------------------------------
    // Admin approves transaction
    // ---------------------------------------------------------

    @PostMapping("/{reviewId}/approve")
    public TransactionReviewResponse approveReview(
            @PathVariable Long reviewId,
            @RequestBody(required = false)
            ReviewCommentRequest request) {

        String comment = null;

        if (request != null) {
            comment = request.getComment();
        }

        return reviewService.approveReview(
                reviewId,
                comment);
    }

    // ---------------------------------------------------------
    // Admin rejects transaction
    // ---------------------------------------------------------

    @PostMapping("/{reviewId}/reject")
    public TransactionReviewResponse rejectReview(
            @PathVariable Long reviewId,
            @RequestBody(required = false)
            ReviewCommentRequest request) {

        String comment = null;

        if (request != null) {
            comment = request.getComment();
        }

        return reviewService.rejectReview(
                reviewId,
                comment);
    }
}