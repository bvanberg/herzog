package com.herzog.api.photo;

import java.util.UUID;

/**
 * Simple static utility for generating a unique photo key.
 *
 * TODO: This needs some work to be really unique and appropriate for storing photos. Also, need to be able to find
 * TODO: these things again. Maybe?
 */
public class UniquePhotoKey {
    public static String get() {
        return UUID.randomUUID().toString();
    }
}
