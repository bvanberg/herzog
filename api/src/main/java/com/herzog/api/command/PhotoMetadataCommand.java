package com.herzog.api.command;

import com.herzog.api.photo.store.PhotoMetadata;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO: Implement as hystrix command.
 */
@Slf4j
public class PhotoMetadataCommand {

    private final PhotoMetadata metadata;

    public PhotoMetadataCommand(final PhotoMetadata metadata) {
        this.metadata = metadata;
    }

    public boolean run() {
        log.info("Got some metadata {}", metadata);

        // Check required fields. Minimally, we must submit the file identifier in S3.
//        Preconditions.checkNotNull(metadata.getUserId());
//        Preconditions.checkState(!metadata.getPhotoKeys().isEmpty());

        // TODO: link up with photo, drop on kafka, and persist in persistent store.

        return true;
    }
}
