package com.loopers.batch.client;

public class KakaoMobilityApiException extends RuntimeException {

    public KakaoMobilityApiException(String message) {
        super(message);
    }

    public KakaoMobilityApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
