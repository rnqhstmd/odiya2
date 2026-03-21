package com.loopers.batch.client;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(BatchKakaoMobilityProperties.class)
public class BatchKakaoMobilityClientConfig {

    @Bean
    public RestClient batchKakaoMobilityRestClient(BatchKakaoMobilityProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeoutSeconds() * 1000);
        factory.setReadTimeout(properties.readTimeoutSeconds() * 1000);

        return RestClient.builder()
            .baseUrl(properties.apiUrl())
            .defaultHeader("Authorization", "KakaoAK " + properties.restApiKey())
            .requestFactory(factory)
            .build();
    }
}
