package com.herzog.api.s3;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.joda.time.DateTime;

import java.net.URL;
import java.util.Date;

/**
 * Simple static generator of presigned URLs for S3.
 *
 * TODO: This is a static utility class for simplification ATM. In the future it would be nice to leverage guice and
 * TODO: proper dependencies.
 */
public class PresignedUrl {
    private static final AmazonS3 S3_CLIENT = new AmazonS3Client(new ProfileCredentialsProvider());
    private static final String BUCKET_NAME = "herzog-photos";

    public static URL from(final String key) {
        java.util.Date expiration = getExpiration();

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(BUCKET_NAME, key);
        generatePresignedUrlRequest.setMethod(HttpMethod.PUT);
        generatePresignedUrlRequest.setExpiration(expiration);

        return S3_CLIENT.generatePresignedUrl(generatePresignedUrlRequest);
    }

    private static Date getExpiration() {
        return DateTime.now().plusHours(1).toDate();
    }
}
