package com.example.s3client.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.s3client.domain.ResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Vyacheslav Aleferov
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    public static final String ORIGINAL_FILE_NAME = "originalFileName";
    private final AmazonS3 s3Client;

    @Value("${aws.bucket}")
    private String bucketName;

    @Value("${aws.endpoint}")
    private String endpoint;

    public UUID upload(MultipartFile file) throws IOException {
        if (!s3Client.doesBucketExistV2(bucketName)) {
            log.info("Bucket creating: " + bucketName);
            s3Client.createBucket(bucketName);
            log.info("Bucket created: " + bucketName);
        }
        log.info("Uploading file '{}' to bucket '{}'", file.getOriginalFilename(), bucketName);
        UUID result = createFileOnS3(file);
        log.info("File '{}' uploaded to bucket '{}' as '{}'", file.getOriginalFilename(), bucketName, result);
        return result;
    }

    public ResponseModel download(String fileName) throws IOException {
        log.info("Downloading file '{}' from bucket '{}'", fileName, bucketName);
        S3Object object = s3Client.getObject(bucketName, fileName);
        log.info("File '{}' downloaded from bucket '{}'", fileName, bucketName);
        HttpUrl.Builder builder = getBuilder();
        String encodedName = object.getObjectMetadata().getUserMetaDataOf(ORIGINAL_FILE_NAME);
        String decodeName = builder.encodedUsername(encodedName).build().username();
        return ResponseModel.builder()
                .nameDecoded(decodeName)
                .nameEncoded(encodedName)
                .data(object.getObjectContent().readAllBytes()).build();
    }

    public Map<String, Long> printAllFiles() {
        Map<String, Long> result = new LinkedHashMap<>();
        ObjectListing objectListing = s3Client.listObjects(new ListObjectsRequest().withBucketName(bucketName));
        log.info("Get files from bucket: {}", bucketName);
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            log.info("{} (size = {})", objectSummary.getKey(), objectSummary.getSize());
            result.put(objectSummary.getKey(), objectSummary.getSize());
        }
        return result;
    }

    public void delete(String fileName) {
        log.info("Deleting file: {}" + fileName);
        s3Client.deleteObject(bucketName, fileName);
        log.info("File {} deleted" + fileName);
    }

    private UUID createFileOnS3(MultipartFile file) throws IOException {
        ObjectMetadata objectMetadata = makeObjectMetadata(file);
        UUID uniqueFileName = createFileName(file);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(file.getBytes());
        s3Client.putObject(new PutObjectRequest(
                bucketName,
                uniqueFileName.toString(),
                inputStream,
                objectMetadata));
        return uniqueFileName;
    }

    private UUID createFileName(MultipartFile file) {
        return UUID.randomUUID();
    }

    private ObjectMetadata makeObjectMetadata(MultipartFile file) {
        HashMap<String, String> userData = new HashMap<>();
        ObjectMetadata objectMetadata = new ObjectMetadata();
        HttpUrl.Builder builder = getBuilder();
        String encodedName = builder.username(file.getOriginalFilename()).build().encodedUsername();
        userData.put(ORIGINAL_FILE_NAME, encodedName);
        objectMetadata.setUserMetadata(userData);
        return objectMetadata;
    }

    private HttpUrl.Builder getBuilder() {
        return Objects.requireNonNull(HttpUrl.parse(endpoint)).newBuilder();
    }

}
