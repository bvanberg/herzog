package com.herzog.module;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.herzog.api.IdentificationConfiguration;

public class HerzogApiModule implements Module {

	@Override
	public void configure(Binder binder) {
	}

	@Provides
	@Named("photoBucket")
	public String provideMessage(IdentificationConfiguration identificationConfiguration) {
		return identificationConfiguration.getPhotoBucket();
	}

	@Provides
	@Singleton
	public AmazonS3 provideAmazonS3Client() {
		return new AmazonS3Client();
	}

}
