package com.herzog.api.photo.store;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.Tolerate;

import java.util.Map;

/**
 * Photo metadata container - metadata is currently just key value pairs.
 */
@Builder
@Getter
@ToString
public class PhotoMetadata {
    @Singular("metadata")
    private Map<String, String> metadata;

    @Tolerate
    public PhotoMetadata() {}
}
