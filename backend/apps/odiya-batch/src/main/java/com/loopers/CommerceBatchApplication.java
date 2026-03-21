package com.loopers;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;
import java.util.TimeZone;

@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication
public class CommerceBatchApplication {

    @PostConstruct
    public void started() {
        // set timezone
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CommerceBatchApplication.class, args);

        boolean isSchedulerMode = Arrays.stream(context.getEnvironment().getActiveProfiles())
            .anyMatch(profile -> profile.equalsIgnoreCase("scheduler"));

        if (!isSchedulerMode) {
            int exitCode = SpringApplication.exit(context);
            System.exit(exitCode);
        }
    }
}
