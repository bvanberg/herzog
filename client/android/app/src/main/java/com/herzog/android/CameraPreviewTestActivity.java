package com.herzog.android;

import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;

import java.util.List;

/**
 * A more advanced preview activity showing some configurable options to update the preview on
 * the go.
 */
public class CameraPreviewTestActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    /**
     * the preview view
     */
    private ResizableCameraPreview mPreview;

    /**
     * an adapter for the spinner options, this is how Android rolls
     */
    private ArrayAdapter<String> mAdapter;

    /**
     * some parent view, whatever that might be, as long as it's a {@link ViewGroup} it's OK. It
     * still has to be a viewgroup since we want to add our preview to it
     */
    private ViewGroup mParentView;

    /**
     * selected camera ID
     */
    private int mCameraId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide status-bar
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide title-bar, must be before setContentView
        // requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.test);

        // Spinner for preview sizes
        Spinner spinnerSize = (Spinner) findViewById(R.id.spinner_size);
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSize.setAdapter(mAdapter);
        spinnerSize.setOnItemSelectedListener(this);

        // Spinner for camera ID
        Spinner spinnerCamera = (Spinner) findViewById(R.id.spinner_camera);
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCamera.setAdapter(adapter);
        spinnerCamera.setOnItemSelectedListener(this);
        // why would we want a fixed list of camaera's here!!?? replaced by a number that matches
        // the amonut of available camera's
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            for (int i = 0; i < Camera.getNumberOfCameras(); i++)
                adapter.add(String.valueOf(i));
        }
        // otherwise fixed list with at least 3 options (this was original code)
        else {
            adapter.add("0");
            adapter.add("1");
            adapter.add("2");
        }

        // any viewgroup can be used from now on
        mParentView = (ViewGroup) findViewById(R.id.surfaceView);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.w("CameraPreviewTestActivity", "onItemSelected invoked");
        Log.w("CameraPreviewTestActivity", "position: " + position);
        Log.w("CameraPreviewTestActivity", "parent.getId(): " + parent.getId());
        switch (parent.getId()) {
            // handle camera size updates
            case R.id.spinner_size:
                Rect rect = new Rect();
                // FIXME we always start from the parent to determine the new size but the parent view
                // is configured to wrap content so gets smaller and smaller as we adapt the size.
                // get the dimensions
                mParentView.getDrawingRect(rect);

                if (0 == position) { // "Auto" selected
                    mPreview.surfaceChanged(null, 0, rect.width(), rect.height());
                } else {
                    mPreview.setPreviewSize(position - 1, rect.width(), rect.height());
                }
                break;
            // handle camera changes
            case R.id.spinner_camera:
                mPreview.stop();
                mParentView.removeView(mPreview);
                mCameraId = position;
                createCameraPreview();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
    }

    @Override
    protected void onResume() {
        super.onResume();
        createCameraPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
        mParentView.removeView(mPreview);
        mPreview = null;
    }

    private void createCameraPreview() {
        // Set the second argument by your choice.
        // Usually, 0 for back-facing camera, 1 for front-facing camera.
        // If the OS is pre-gingerbreak, this does not have any effect.
        mPreview = new ResizableCameraPreview(this, mCameraId, false);
        LayoutParams previewLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        // if we add the preview view again the layout stays OK
        mParentView.addView(mPreview, 0, previewLayoutParams);

        // update available sizes according to selected camera
        mAdapter.clear();
        mAdapter.add("Auto");
        List<Camera.Size> sizes = mPreview.getSupportedPreivewSizes();
        for (Camera.Size size : sizes) {
            mAdapter.add(size.width + " x " + size.height);
        }
    }
}
