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
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * A preview view that will match the dimensions to fit the parent view. Use the parent view to
 * control the size of the preview screen. The aspect ratio is managed within this view.
 * <p/>
 * The parent view no longer has to be a RelativeLayout, it just needs to be a ViewGroup now.
 * TODO check if we even need a wrapping view, why not have a surfaceView defined in the layout?
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String LOG_TAG = CameraPreview.class.getSimpleName();

    private static final String CAMERA_PARAM_ORIENTATION = "orientation";
    private static final String CAMERA_PARAM_LANDSCAPE = "landscape";
    private static final String CAMERA_PARAM_PORTRAIT = "portrait";

    protected Activity mActivity;
    private SurfaceHolder mHolder;
    protected Camera mCamera;

    protected List<Camera.Size> mPreviewSizeList, mPictureSizeList;

    protected Camera.Size mPreviewSize, mPictureSize;

    private int mSurfaceChangedCallDepth = 0, mCameraId; //, mCenterPosX = -1, mCenterPosY;

    private PreviewReadyCallback mPreviewReadyCallback = null;

    public interface PreviewReadyCallback {
        public void onPreviewReady();
    }

    /**
     * State flag: true when surface's layout size is set and surfaceChanged()
     * process has not been completed.
     */
    protected boolean mSurfaceConfiguring = false;

    /**
     * def ctor
     *
     * @param activity
     * @param cameraId
     */
    public CameraPreview(Activity activity, int cameraId) {
        super(activity); // Always necessary
        mActivity = activity;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // opening camera has changed over API revisions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            // just a safety check before setting camera ID
            mCameraId = (Camera.getNumberOfCameras() > cameraId) ? cameraId : 0;
            mCamera = Camera.open(mCameraId);
        } else {
            mCameraId = 0;
            mCamera = Camera.open();
        }

        // retrieve information about available camera features
        Camera.Parameters cameraParams = mCamera.getParameters();
        mPreviewSizeList = cameraParams.getSupportedPreviewSizes();
        mPictureSizeList = cameraParams.getSupportedPictureSizes();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(LOG_TAG, "surface created");
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
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
        mSurfaceChangedCallDepth++;
        doSurfaceChanged(width, height);
        mSurfaceChangedCallDepth--;
    }

    /**
     * helper to handle surface changes
     *
     * @param width
     * @param height
     */
    private void doSurfaceChanged(int width, int height) {

        // stop the preview
        mCamera.stopPreview();

        // check the params again
        Camera.Parameters cameraParams = mCamera.getParameters();
        boolean portrait = isPortrait();

        // The code in this if-statement is prevented from executed again when surfaceChanged is
        // called again due to the change of the layout size in this if-statement.
        if (!mSurfaceConfiguring) {
            Camera.Size previewSize = determinePreviewSize(portrait, width, height);
            Camera.Size pictureSize = determinePictureSize(previewSize);
            Log.d(LOG_TAG, "Desired Preview Size - w: " + width + ", h: " + height);
            mPreviewSize = previewSize;
            mPictureSize = pictureSize;
            mSurfaceConfiguring = adjustSurfaceLayoutSize(previewSize, portrait, width, height);
            // Continue executing this method if this method is called recursively.
            // Recursive call of surfaceChanged is very special case, which is a path from
            // the catch clause at the end of this method.
            // The later part of this method should be executed as well in the recursive
            // invocation of this method, because the layout change made in this recursive
            // call will not trigger another invocation of this method.
            if (mSurfaceConfiguring && (mSurfaceChangedCallDepth <= 1)) {
                return;
            }
        }

        configureCameraParameters(cameraParams, portrait);
        mSurfaceConfiguring = false;

        // try to start the preview again now that params have been updated
        try {
            mCamera.startPreview();
        } catch (Exception e) {
            Log.w(LOG_TAG, "Failed to start preview: " + e.getMessage());

            // Remove failed size
            mPreviewSizeList.remove(mPreviewSize);
            mPreviewSize = null;

            // Reconfigure
            if (mPreviewSizeList.size() > 0) { // prevent infinite loop
                surfaceChanged(null, 0, width, height);
            } else {
                Toast.makeText(mActivity, "Can't start preview", Toast.LENGTH_LONG).show();
                Log.w(LOG_TAG, "Gave up starting preview");
            }
        }

        // notify callback
        if (null != mPreviewReadyCallback) {
            mPreviewReadyCallback.onPreviewReady();
        }
    }

    /**
     * helper to calculate the preview size
     *
     * @param portrait
     * @param reqWidth  must be the value of the parameter passed in surfaceChanged
     * @param reqHeight must be the value of the parameter passed in surfaceChanged
     * @return Camera.Size object that is an element of the list returned from Camera.Parameters.getSupportedPreviewSizes.
     */
    protected Camera.Size determinePreviewSize(boolean portrait, int reqWidth, int reqHeight) {
        // Meaning of width and height is switched for preview when portrait,
        // while it is the same as user's view for surface and metrics.
        // That is, width must always be larger than height for setPreviewSize.
        int reqPreviewWidth; // requested width in terms of camera hardware
        int reqPreviewHeight; // requested height in terms of camera hardware

        // handle portrait
        if (portrait) {
            reqPreviewWidth = reqHeight;
            reqPreviewHeight = reqWidth;
        } else {
            reqPreviewWidth = reqWidth;
            reqPreviewHeight = reqHeight;
        }

        // debugging of all options
        // commented since this takes time
        //Log.d(LOG_TAG, "Listing all supported preview sizes");
        //for (Camera.Size size : mPreviewSizeList) {
        //    Log.d(LOG_TAG, "  w: " + size.width + ", h: " + size.height);
        //}
        //Log.d(LOG_TAG, "Listing all supported picture sizes");
        //for (Camera.Size size : mPictureSizeList) {
        //    Log.d(LOG_TAG, "  w: " + size.width + ", h: " + size.height);
        //}

        // Adjust surface size with the closest aspect-ratio
        float reqRatio = ((float) reqPreviewWidth) / reqPreviewHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : mPreviewSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }

        return retSize;
    }

    /**
     * helper to calculate the picture size
     *
     * @param previewSize
     * @return
     */
    protected Camera.Size determinePictureSize(Camera.Size previewSize) {
        Camera.Size retSize = null;

        // try to return the exact same size
        for (Camera.Size size : mPictureSizeList) {
            if (size.equals(previewSize)) {
                return size;
            }
        }

        Log.d(LOG_TAG, "Same picture size not found.");

        // if the preview size is not supported as a picture size
        float reqRatio = ((float) previewSize.width) / previewSize.height;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        for (Camera.Size size : mPictureSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }

        return retSize;
    }

    /**
     * perform updates to size according to layout changes
     *
     * @param previewSize
     * @param portrait
     * @param availableWidth
     * @param availableHeight
     * @return
     */
    protected boolean adjustSurfaceLayoutSize(Camera.Size previewSize, boolean portrait,
                                              int availableWidth, int availableHeight) {
        float tmpLayoutHeight, tmpLayoutWidth;

        // again handle portrait mode
        if (portrait) {
            tmpLayoutHeight = previewSize.width;
            tmpLayoutWidth = previewSize.height;
        } else {
            tmpLayoutHeight = previewSize.height;
            tmpLayoutWidth = previewSize.width;
        }

        float factH, factW, fact;
        factH = availableHeight / tmpLayoutHeight;
        factW = availableWidth / tmpLayoutWidth;

        // Select smaller factor, because the surface cannot be set to the size larger than display metrics.
        if (factH < factW) {
            fact = factH;
        } else {
            fact = factW;
        }

        ViewGroup.LayoutParams layoutParams = this.getLayoutParams();

        int layoutHeight = (int) (tmpLayoutHeight * fact);
        int layoutWidth = (int) (tmpLayoutWidth * fact);
        Log.d(LOG_TAG, "Preview Layout Size - w: " + layoutWidth + ", h: " + layoutHeight);
        Log.d(LOG_TAG, "Scale factor: " + fact);

        boolean layoutChanged;
        if ((layoutWidth != this.getWidth()) || (layoutHeight != this.getHeight())) {
            layoutParams.height = layoutHeight;
            layoutParams.width = layoutWidth;
            // centering is no longer done in this view, instead we rely on the parent view to
            // perform the centering
            //if (mCenterPosX >= 0) {
            //    layoutParams.topMargin = mCenterPosY - (layoutHeight / 2);
            //    layoutParams.leftMargin = mCenterPosX - (layoutWidth / 2);
            //}
            this.setLayoutParams(layoutParams); // this will trigger another surfaceChanged invocation.
            layoutChanged = true;
        } else {
            layoutChanged = false;
        }

        return layoutChanged;
    }

    /**
     * @param x X coordinate of center position on the screen. Set to negative value to unset.
     * @param y Y coordinate of center position on the screen.
     *
    public void setCenterPosition(int x, int y) {
        mCenterPosX = x;
        mCenterPosY = y;
    }*/

    /**
     * helper to update camaera params
     *
     * @param cameraParams
     * @param portrait
     */
    protected void configureCameraParameters(Camera.Parameters cameraParams, boolean portrait) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) { // for 2.1 and before
            if (portrait) {
                cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_PORTRAIT);
            } else {
                cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_LANDSCAPE);
            }
        } else { // for 2.2 and later
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
