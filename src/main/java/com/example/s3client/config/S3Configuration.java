package com.example.s3client.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Vyacheslav Aleferov
 */
@Configuration
@Getter
@Setter
public class S3Configuration {

    @Value("${aws.endpoint}")
    private String s3Endpoint;

    @Value("${aws.accessKeyId}")
    private String s3AccessKey;

    @Value("${aws.secretKey}")
    private String s3SecretKey;

    @Value("${aws.region}")
    private String region;

    @Bean
    public AmazonS3 getS3Client() {
        AWSCredentials awsCredentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
        ClientConfiguration clientConfiguration = new ClientConfiguration()
                .withSignerOverride("S3SignerType")
                .withProtocol(Protocol.HTTP);
        return AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s3Endpoint, Regions.fromName(region).getName()))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }
}
