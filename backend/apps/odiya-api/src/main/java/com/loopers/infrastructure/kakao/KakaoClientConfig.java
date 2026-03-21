package com.loopers.infrastructure.kakao;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(KakaoProperties.class)
public class KakaoClientConfig {

    @Bean
    public RestClient kakaoRestClient(KakaoProperties kakaoProperties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(kakaoProperties.connectTimeoutSeconds() * 1000);
        factory.setReadTimeout(kakaoProperties.readTimeoutSeconds() * 1000);

        return RestClient.builder()
            .baseUrl(kakaoProperties.apiUrl())
            .requestFactory(factory)
            .build();
    }
}
