package com.herzog.api;

import com.herzog.api.photo.store.Photo;
import lombok.Builder;
import lombok.Getter;

import java.util.Collection;

/**
 * Simple photo aggregation.
 */
@Builder
@Getter
public class PhotoList {
    private final Collection<Photo> photos;
}
