
package com.sujitha.aipaymentfrauddetection.service;

import com.sujitha.aipaymentfrauddetection.dto.MlPredictionRequest;
import com.sujitha.aipaymentfrauddetection.dto.MlPredictionResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class MlPredictionService {

    private final RestClient restClient;

    public MlPredictionService(
            @Value("${ml.service.url:http://127.0.0.1:8000}") String mlServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(mlServiceUrl)
                .build();
    }

    public MlPredictionResponse predict(MlPredictionRequest request) {

        return restClient.post()
                .uri("/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(MlPredictionResponse.class);
    }
}
