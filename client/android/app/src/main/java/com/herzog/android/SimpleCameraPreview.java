package com.herzog.android;

import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * A simplified version of the preview class
 * <p/>
 * TODO merge with the normal one (use extend)
 * <p/>
 * A preview view that will match the dimensions to fit the parent view. Use the parent view to
 * control the size of the preview screen. The aspect ratio is managed within this view.
 * <p/>
 * The parent view no longer has to be a RelativeLayout, it just needs to be a ViewGroup now.
 */
public class SimpleCameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String LOG_TAG = SimpleCameraPreview.class.getSimpleName();

    private static final String CAMERA_PARAM_ORIENTATION = "orientation";
    private static final String CAMERA_PARAM_LANDSCAPE = "landscape";
    private static final String CAMERA_PARAM_PORTRAIT = "portrait";

    protected Activity mActivity;
    private SurfaceHolder mHolder;
    protected Camera mCamera;

    protected List<Camera.Size> mPreviewSizeList, mPictureSizeList;

    protected Camera.Size mPreviewSize, mPictureSize;

    /**
     * def ctor
     *
     * @param activity
     */
    public SimpleCameraPreview(Activity activity) {
        super(activity); // Always necessary
        mActivity = activity;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // opening camera has changed over API revisions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            // just a safety check before setting camera ID
            mCamera = Camera.open(0);
        } else {
            mCamera = Camera.open();
        }

        // retrieve information about available camera features
        Camera.Parameters cameraParams = mCamera.getParameters();
        //mPreviewSizeList = cameraParams.getSupportedPreviewSizes();
        mPictureSizeList = cameraParams.getSupportedPictureSizes();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(LOG_TAG, "surface created");
        try {
            mCamera.setPreviewDisplay(mHolder);

            // set all params
            Camera.Parameters cameraParams = mCamera.getParameters();
            boolean portrait = isPortrait();

            int width = holder.getSurfaceFrame().width();
            int height = holder.getSurfaceFrame().height();

            Camera.Size previewSize  = getOptimalPreviewSize(mPictureSizeList, width, height);

            Log.d(LOG_TAG, "Desired Preview Size - w: " + width + ", h: " + height);
            mPreviewSize = previewSize;
            mPictureSize = previewSize;//pictureSize;

            configureCameraParameters(cameraParams, portrait);

            mCamera.startPreview();

        } catch (IOException e) {
            mCamera.release();
            mCamera = null;

            Log.w(LOG_TAG, "Failed to start preview: " + e.getMessage());

            // Remove failed size
            mPreviewSizeList.remove(mPreviewSize);
            mPreviewSize = null;
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(LOG_TAG, "surface destroyed");
        stop();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(LOG_TAG, "surface changed");
    }

    /**
     * helper to retrieve best fitting dimensions
     *
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    /**
     * helper to update camera params
     *
     * @param cameraParams
     * @param portrait
     */
    protected void configureCameraParameters(Camera.Parameters cameraParams, boolean portrait) {

        // for 2.1 and before
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            if (portrait) {
                cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_PORTRAIT);
            } else {
                cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_LANDSCAPE);
            }
        }
        // for 2.2 and later
        else {
            int angle;
            Display display = mActivity.getWindowManager().getDefaultDisplay();
            switch (display.getRotation()) {
                case Surface.ROTATION_0: // This is display orientation
                    angle = 90; // This is camera orientation
                    break;
                case Surface.ROTATION_90:
                    angle = 0;
                    break;
                case Surface.ROTATION_180:
                    angle = 270;
                    break;
                case Surface.ROTATION_270:
                    angle = 180;
                    break;
                default:
                    angle = 90;
                    break;
            }
            Log.v(LOG_TAG, "angle: " + angle);
            mCamera.setDisplayOrientation(angle);
        }

        cameraParams.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        cameraParams.setPictureSize(mPictureSize.width, mPictureSize.height);
        Log.d(LOG_TAG, "Preview Actual Size - w: " + mPreviewSize.width + ", h: " + mPreviewSize.height);
        Log.d(LOG_TAG, "Picture Actual Size - w: " + mPictureSize.width + ", h: " + mPictureSize.height);

        mCamera.setParameters(cameraParams);
    }

    /**
     * helper for stopping camera and cleaning up references
     */
    public void stop() {
        if (null == mCamera) {
            return;
        }
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    /**
     * check if camera is in portrait mode or not
     *
     * @return
     */
    public boolean isPortrait() {
        return (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    }

}
