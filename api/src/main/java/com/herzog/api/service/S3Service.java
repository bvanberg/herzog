package com.herzog.api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class S3Service {

    private final AmazonS3 s3Client;
    private final String photoBucket;

    @Inject
    public S3Service(final AmazonS3 s3Client, final @Named("photoBucket") String photoBucket) {
        this.s3Client = s3Client;
        this.photoBucket = photoBucket;
    }
}