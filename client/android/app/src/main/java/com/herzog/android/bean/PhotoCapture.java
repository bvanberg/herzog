package com.herzog.android.bean;

import java.io.File;
import java.io.Serializable;

public class PhotoCapture implements Serializable {

    private final File photo;
    private final String description;

    public PhotoCapture(File photo, String description) {
        this.photo = photo;
        this.description = description;
    }

    public File getPhoto() {
        return photo;
    }

    public String getDescription() {
        return description;
    }
}
