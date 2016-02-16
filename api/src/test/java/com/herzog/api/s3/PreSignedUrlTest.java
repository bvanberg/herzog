package com.herzog.api.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.herzog.api.photo.UniquePhotoKey;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PreSignedUrlTest {

    @Ignore
    @Test
    public void should_upload_file_with_pre_signed_url() throws Exception {

        // setup test
        final String filename = "coffee-mug.jpg";
        final String s3Filename = UniquePhotoKey.get() + filename.substring(filename.lastIndexOf("."));
        PresignedUrl presignedUrl = new PresignedUrl(new AmazonS3Client());
        final URL preSignedUrlForUpload = presignedUrl.from(s3Filename);

        // run test
        final HttpURLConnection connection = (HttpURLConnection) preSignedUrlForUpload.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");

        final InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        final OutputStream os = connection.getOutputStream();

        final int bytesCopied = IOUtils.copy(is, os);
        os.close();

        int responseCode = connection.getResponseCode();

        System.out.println("bytes copied " + bytesCopied);
        System.out.println("Service returned response code " + responseCode);

    }
}
