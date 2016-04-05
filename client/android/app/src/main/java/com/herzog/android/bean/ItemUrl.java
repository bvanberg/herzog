package com.herzog.android.bean;

import java.io.Serializable;

public class ItemUrl implements Serializable {

    private String presignedUrl;
    private String key;

    public String getPresignedUrl() {
        return presignedUrl;
    }

    public void setPresignedUrl(String presignedUrl) {
        this.presignedUrl = presignedUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
