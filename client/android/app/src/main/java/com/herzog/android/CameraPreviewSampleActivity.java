package com.herzog.android;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * A sample camera preview activity that uses no xml configured layout and instead relies on in
 * code configured views.
 */
public class CameraPreviewSampleActivity extends ActionBarActivity {

    /**
     * the actual preview view
     */
    private CameraPreview mPreview;

    /**
     * the wrapping layout view
     */
    private RelativeLayout mParentView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide status-bar
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide title-bar, must be before setContentView
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        // no longer Requires to be RelativeLayout, let's make it one just for fun
        mParentView = new RelativeLayout(this);

        // set a dark background here to hide the fact that this view might not wrap parent
        // completely in order to respect aspect ratio of the camera
        mParentView.setBackgroundColor(getResources().getColor(R.color.background));
        setContentView(mParentView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set the second argument by your choice.
        // Usually, 0 for back-facing camera, 1 for front-facing camera.
        // If the OS is pre-gingerbreak, this does not have any effect.
        mPreview = new CameraPreview(this, 0);
        LayoutParams previewLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        previewLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        // Un-comment below lines to specify the size in code
        //previewLayoutParams.height = 500;
        //previewLayoutParams.width = 500;

        // Un-comment below line to specify the position.
        //mPreview.setCenterPosition(270, 130);

        mParentView.addView(mPreview, 0, previewLayoutParams);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
        mParentView.removeView(mPreview); // This is necessary.
        mPreview = null;
    }
}
