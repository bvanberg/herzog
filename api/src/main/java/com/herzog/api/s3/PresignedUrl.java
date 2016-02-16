package com.herzog.api.s3;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.net.URL;
import java.util.Date;

/**
 * Simple generator of presigned URLs for S3.
 */
public class PresignedUrl {

    private static final String BUCKET_NAME = "herzog-photos";

    private final AmazonS3 amazonS3;

    @Inject
    public PresignedUrl(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public URL from(final String key) {
        java.util.Date expiration = getExpiration();

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(BUCKET_NAME, key);
        generatePresignedUrlRequest.setMethod(HttpMethod.PUT);
        generatePresignedUrlRequest.setExpiration(expiration);
        generatePresignedUrlRequest.withContentType("binary/octet-stream");

        return amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
    }

    private Date getExpiration() {
        return DateTime.now().plusHours(1).toDate();
    }
}
