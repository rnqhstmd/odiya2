package com.loopers.infrastructure.odsay;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OdsayProperties.class)
public class OdsayClientConfig {

    @Bean
    public RestClient odsayRestClient(OdsayProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeoutSeconds() * 1000);
        factory.setReadTimeout(properties.readTimeoutSeconds() * 1000);

        return RestClient.builder()
            .baseUrl(properties.apiUrl())
            .requestFactory(factory)
            .build();
    }
}
