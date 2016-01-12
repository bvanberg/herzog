package com.herzog.api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.io.IOException;
import java.io.InputStream;

public class S3Service {

	private AmazonS3 s3Client;
	private String photoBucket;

	@Inject
	public S3Service(
			final AmazonS3 s3Client,
			final @Named("photoBucket") String photoBucket
	) {
		this.s3Client = s3Client;
		this.photoBucket = photoBucket;
	}

	public void saveFile(
			final InputStream inputStream,
			final long fileSize,
			String filename
	) throws IOException {

		ObjectMetadata metaData = new ObjectMetadata();

		// Content length for the data stream must be specified in the object metadata parameter;
		// Amazon S3 requires it be passed in before the data is uploaded.
		// Failure to specify a content length will cause the entire contents of the input stream
		// to be buffered locally in memory so that the content length can be calculated, which can result in negative performance problem
		metaData.setContentLength(fileSize);

		// Remove the leading / from the filename (if one exists) to make sure the upload to S3 works properly.
		if (filename != null && filename.startsWith("/")) filename = filename.substring(1); // chop off the leading slash

		PutObjectRequest request = new PutObjectRequest(photoBucket, filename, inputStream, metaData);
		request.setCannedAcl(CannedAccessControlList.PublicRead);
		s3Client.putObject(request);
		// todo: potentially return the result or object metadata
	}
}