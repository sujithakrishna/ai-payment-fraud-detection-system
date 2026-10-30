package com.sujitha.aipaymentfrauddetection.service;

import com.sujitha.aipaymentfrauddetection.dto.TransactionReviewResponse;
import com.sujitha.aipaymentfrauddetection.entity.Transaction;
import com.sujitha.aipaymentfrauddetection.entity.TransactionReview;
import com.sujitha.aipaymentfrauddetection.entity.User;
import com.sujitha.aipaymentfrauddetection.repository.TransactionRepository;
import com.sujitha.aipaymentfrauddetection.repository.TransactionReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    private final TransactionReviewRepository reviewRepository;
    private final TransactionRepository transactionRepository;

    public ReviewService(TransactionReviewRepository reviewRepository,
                         TransactionRepository transactionRepository) {
        this.reviewRepository = reviewRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionReview createReview(TransactionReview review) {

        review.setReviewStatus("PENDING");

        if (review.getCreatedAt() == null) {
            review.setCreatedAt(LocalDateTime.now());
        }

        return reviewRepository.save(review);
    }

    public TransactionReviewResponse getReviewByTransactionId(Long transactionId) {

        User currentUser = getCurrentUser();

        TransactionReview review = reviewRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Review not found"));

        // ADMIN can view any review
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return toResponse(review);
        }

        // USER can view only their own review
        if (!currentUser.getUserId().equals(review.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to access this review");
        }

        return toResponse(review);
    }

    public List<TransactionReviewResponse> getPendingReviews() {

        requireAdmin();

        return reviewRepository
                .findByReviewStatusOrderByCreatedAtAsc("PENDING")
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TransactionReviewResponse addUserDescription(
            Long reviewId,
            String description) {

        User currentUser = getCurrentUser();

        TransactionReview review = reviewRepository
                .findById(reviewId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Review not found"));

        // Admin cannot submit a user description
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admins cannot submit user descriptions");
        }

        // User can modify only their own review
        if (!currentUser.getUserId().equals(review.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to update this review");
        }

        if (!"PENDING".equalsIgnoreCase(review.getReviewStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Review is no longer pending");
        }

        if (description == null || description.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Description cannot be empty");
        }

        review.setUserDescription(description.trim());

        review = reviewRepository.save(review);

        return toResponse(review);
    }

    @Transactional
    public TransactionReviewResponse approveReview(
            Long reviewId,
            String adminComment) {

        requireAdmin();

        TransactionReview review = reviewRepository
                .findById(reviewId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Review not found"));

        if (!"PENDING".equalsIgnoreCase(review.getReviewStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Review is no longer pending");
        }

        Transaction transaction = transactionRepository
                .findById(review.getTransactionId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Transaction not found"));

        transaction.setTransactionStatus("SUCCESS");

        review.setReviewStatus("APPROVED");

        review.setAdminComment(
                adminComment == null
                        ? null
                        : adminComment.trim()
        );

        review.setReviewedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        review = reviewRepository.save(review);

        return toResponse(review);
    }

    @Transactional
    public TransactionReviewResponse rejectReview(
            Long reviewId,
            String adminComment) {

        requireAdmin();

        TransactionReview review = reviewRepository
                .findById(reviewId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Review not found"));

        if (!"PENDING".equalsIgnoreCase(review.getReviewStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Review is no longer pending");
        }

        Transaction transaction = transactionRepository
                .findById(review.getTransactionId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Transaction not found"));

        transaction.setTransactionStatus("REJECTED");

        review.setReviewStatus("REJECTED");

        review.setAdminComment(
                adminComment == null
                        ? null
                        : adminComment.trim()
        );

        review.setReviewedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        review = reviewRepository.save(review);

        return toResponse(review);
    }

    /*
     * Gets the currently authenticated user from Spring Security.
     *
     * Diagnostic version:
     * This tells us whether the authentication object reaches
     * ReviewService and whether its principal is our User entity.
     */
    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "REVIEW_SERVICE_AUTHENTICATION_IS_NULL");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "REVIEW_SERVICE_WRONG_PRINCIPAL_TYPE: "
                        + principal.getClass().getName());
    }

    private void requireAdmin() {

        User currentUser = getCurrentUser();

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admin access is required");
        }
    }

    private TransactionReviewResponse toResponse(
            TransactionReview review) {

        TransactionReviewResponse response =
                new TransactionReviewResponse();

        response.setReviewId(review.getReviewId());
        response.setTransactionId(review.getTransactionId());
        response.setUserId(review.getUserId());
        response.setRiskScore(review.getRiskScore());
        response.setRiskLevel(review.getRiskLevel());
        response.setRiskReason(review.getRiskReason());
        response.setUserDescription(review.getUserDescription());
        response.setReviewStatus(review.getReviewStatus());
        response.setAdminComment(review.getAdminComment());
        response.setReviewedAt(review.getReviewedAt());
        response.setCreatedAt(review.getCreatedAt());

        return response;
    }
}