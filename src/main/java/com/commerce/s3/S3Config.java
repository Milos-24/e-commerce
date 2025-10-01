package com.commerce.s3;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.EU_NORTH_1)
                .build();
    }


    @Bean
    public S3Presigner s3Presigner(S3Client s3Client) {
        return S3Presigner.builder()
                .region(Region.of(Region.EU_NORTH_1.toString()))
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

}
