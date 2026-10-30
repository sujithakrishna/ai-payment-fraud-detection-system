
package com.sujitha.aipaymentfrauddetection.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MlPredictionResponse {

    private String prediction;

    @JsonProperty("fraud_probability")
    private double fraudProbability;

    private String model;

    @JsonProperty("training_type")
    private String trainingType;

    private double threshold;

    public MlPredictionResponse() {
    }

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public double getFraudProbability() {
        return fraudProbability;
    }

    public void setFraudProbability(double fraudProbability) {
        this.fraudProbability = fraudProbability;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(String trainingType) {
        this.trainingType = trainingType;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}
