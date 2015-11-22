package com.herzog.api.photo.store;

import lombok.Builder;
import lombok.Getter;

/**
 * Simple photo pojo
 */
@Builder
@Getter
public class Photo {
    private final String url;
}
