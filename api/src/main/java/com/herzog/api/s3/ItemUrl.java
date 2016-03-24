package com.herzog.api.s3;

import lombok.Builder;
import lombok.Getter;

/**
 * Item presigned URL and item id.
 */
@Builder
@Getter
public class ItemUrl {
    private final String presignedUrl;
    private final String key;
}
