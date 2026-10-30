
package com.sujitha.aipaymentfrauddetection.dto;

import java.math.BigDecimal;

public class MlPredictionRequest {

    private BigDecimal amount;
    private String transaction_type;
    private String payment_method;
    private int sender_txn_count_10m;
    private int receiver_txn_count_10m;
    private double minutes_since_last_txn;
    private int location_changed;
    private int transaction_hour;

    public MlPredictionRequest(
            BigDecimal amount,
            String transaction_type,
            String payment_method,
            int sender_txn_count_10m,
            int receiver_txn_count_10m,
            double minutes_since_last_txn,
            int location_changed,
            int transaction_hour) {

        this.amount = amount;
        this.transaction_type = transaction_type;
        this.payment_method = payment_method;
        this.sender_txn_count_10m = sender_txn_count_10m;
        this.receiver_txn_count_10m = receiver_txn_count_10m;
        this.minutes_since_last_txn = minutes_since_last_txn;
        this.location_changed = location_changed;
        this.transaction_hour = transaction_hour;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTransaction_type() {
        return transaction_type;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public int getSender_txn_count_10m() {
        return sender_txn_count_10m;
    }

    public int getReceiver_txn_count_10m() {
        return receiver_txn_count_10m;
    }

    public double getMinutes_since_last_txn() {
        return minutes_since_last_txn;
    }

    public int getLocation_changed() {
        return location_changed;
    }

    public int getTransaction_hour() {
        return transaction_hour;
    }
}
