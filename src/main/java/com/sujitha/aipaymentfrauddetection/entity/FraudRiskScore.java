package com.sujitha.aipaymentfrauddetection.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "fraud_risk_scores")
public class FraudRiskScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "risk_id")
    private Long riskId;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Column(name = "risk_score")
    private BigDecimal riskScore;

    @Column(name = "risk_level")
    private String riskLevel;

    @Column(name = "risk_reason")
    private String riskReason;

    @Column(name = "detection_method")
    private String detectionMethod;

    public FraudRiskScore() {
    }

    public Long getRiskId() {
        return riskId;
    }

    public void setRiskId(Long riskId) {
        this.riskId = riskId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRiskReason() {
        return riskReason;
    }

    public void setRiskReason(String riskReason) {
        this.riskReason = riskReason;
    }

    public String getDetectionMethod() {
        return detectionMethod;
    }

    public void setDetectionMethod(String detectionMethod) {
        this.detectionMethod = detectionMethod;
    }
}