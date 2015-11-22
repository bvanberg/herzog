package com.herzog.android.zxing;

import android.os.Bundle;

import java.io.Serializable;

/**
 * This is how we get results back from fragment
 */
public interface ResultHandler extends Serializable {

    public void handleResult(Bundle data);
}
