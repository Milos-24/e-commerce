package com.commerce.shared.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Buckets s3Buckets;
    private final S3Presigner s3Presigner;

    public void putObject(String bucketName, String key, byte[] file) {
        s3Client.putObject(
                PutObjectRequest.builder().bucket(bucketName).key(key).build(),
                RequestBody.fromBytes(file));
    }

    public byte[] getObject(String bucketName, String key) {
        ResponseInputStream<GetObjectResponse> result = s3Client.getObject(
                GetObjectRequest.builder().bucket(bucketName).key(key).build());
        try {
            return result.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Cacheable(value = "presigned-urls", key = "#key")
    public String generatePresignedUrl(String bucket, String key) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(key).build())
                .signatureDuration(Duration.ofMinutes(60))
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }
}
