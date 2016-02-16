package com.herzog.api.s3;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.google.inject.name.Named;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.net.URL;
import java.util.Date;

/**
 * Simple generator of presigned URLs for S3.
 */
public class PresignedUrl {

    private final AmazonS3 amazonS3;
    private final String photoBucket;

    @Inject
    public PresignedUrl(final AmazonS3 amazonS3, final @Named("photoBucket") String photoBucket) {
        this.amazonS3 = amazonS3;
        this.photoBucket = photoBucket;
    }

    public URL from(final String key) {
        java.util.Date expiration = getExpiration();

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(photoBucket, key);
        generatePresignedUrlRequest.setMethod(HttpMethod.PUT);
        generatePresignedUrlRequest.setExpiration(expiration);
        generatePresignedUrlRequest.withContentType("binary/octet-stream");

        return amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
    }

    private Date getExpiration() {
        return DateTime.now().plusHours(1).toDate();
    }
}
