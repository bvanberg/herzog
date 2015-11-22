package com.herzog.android;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.util.List;

/**
 * CameraPreview class that is extended only for the purpose of testing CameraPreview class.
 * This class is added functionality to set arbitrary preview size, and removed automated retry
 * function to start preview on exception.
 */
public class ResizableCameraPreview extends CameraPreview {

    /**
     * create log tag, do this only once
     */
    private static final String LOG_TAG = ResizableCameraPreview.class.getSimpleName();

    /**
     * @param activity
     * @param addReversedSizes is set to true to add reversed values of supported preview-sizes to the list.
     */
    public ResizableCameraPreview(Activity activity, int cameraId, boolean addReversedSizes) {
        super(activity, cameraId);
        if (addReversedSizes) {
            List<Camera.Size> sizes = mPreviewSizeList;
            int length = sizes.size();
            for (int i = 0; i < length; i++) {
                Camera.Size size = sizes.get(i);
                Camera.Size revSize = mCamera.new Size(size.height, size.width);
                sizes.add(revSize);
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();

        Camera.Parameters cameraParams = mCamera.getParameters();
        boolean portrait = isPortrait();

        if (!mSurfaceConfiguring) {
            Camera.Size previewSize = determinePreviewSize(portrait, width, height);
            Camera.Size pictureSize = determinePictureSize(previewSize);
            Log.d(LOG_TAG, "Desired Preview Size - w: " + width + ", h: " + height);
            mPreviewSize = previewSize;
            mPictureSize = pictureSize;
            mSurfaceConfiguring = adjustSurfaceLayoutSize(previewSize, portrait, width, height);
            if (mSurfaceConfiguring) {
                return;
            }
        }

        configureCameraParameters(cameraParams, portrait);
        mSurfaceConfiguring = false;

        try {
            mCamera.startPreview();
        } catch (Exception e) {
            Toast.makeText(mActivity, "Failed to start preview: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.w(LOG_TAG, "Failed to start preview: " + e.getMessage());
        }
    }

    /**
     * @param index  selects preview size from the list returned by CameraPreview.getSupportedPreivewSizes().
     * @param width  is the width of the available area for this view
     * @param height is the height of the available area for this view
     */
    public void setPreviewSize(int index, int width, int height) {
        mCamera.stopPreview();

        Camera.Parameters cameraParams = mCamera.getParameters();
        boolean portrait = isPortrait();

        Camera.Size previewSize = mPreviewSizeList.get(index);
        Camera.Size pictureSize = determinePictureSize(previewSize);
        Log.d(LOG_TAG, "Requested Preview Size - w: " + previewSize.width + ", h: " + previewSize.height);
        mPreviewSize = previewSize;
        mPictureSize = pictureSize;
        boolean layoutChanged = adjustSurfaceLayoutSize(previewSize, portrait, width, height);
        if (layoutChanged) {
            mSurfaceConfiguring = true;
            return;
        }

        configureCameraParameters(cameraParams, portrait);
        try {
            mCamera.startPreview();
        } catch (Exception e) {
            Toast.makeText(mActivity, "Failed to satart preview: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        mSurfaceConfiguring = false;
    }

    public List<Camera.Size> getSupportedPreivewSizes() {
        return mPreviewSizeList;
    }
}
