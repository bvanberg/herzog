package com.herzog.api.photo.store;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import lombok.Builder;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

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

    public Collection<Photo> getPhotoPage(final int page, final int pageSize) {
        final Iterable<List<Photo>> partitions = Iterables.partition(photoList, pageSize);

        return Iterables.get(partitions, page, photoList);
    }
}
