package com.herzog.api.photo.store;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Getter;

/**
 * Simple photo store implementation. Eventually this should be backed by a real store that sends back real images.
 */
@Builder
@Getter
public class PhotoStore {
    private final ImmutableList<Photo> photoList = ImmutableList.of(
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build(),
            Photo.builder().url("https://unsplash.it/200/300/?random").build());
}
