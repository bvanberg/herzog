package com.herzog.android.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PhotoMetadata implements Serializable {

    private String userId;
    private List<String> photoKeys;
    private Map<String, String> metadata;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getPhotoKeys() {
        return photoKeys;
    }

    public void setPhotoKeys(List<String> photoKeys) {
        this.photoKeys = photoKeys;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
