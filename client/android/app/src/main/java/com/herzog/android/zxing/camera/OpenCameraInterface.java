package com.herzog.android.zxing.camera;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;

public final class OpenCameraInterface {
	
	private static final String TAG = OpenCameraInterface.class.getName();

	private OpenCameraInterface() {
	}

	/**
	 * Opens a rear-facing camera with {@link android.hardware.Camera#open(int)}
	 * , if one exists, or opens camera 0.
	 * 
	 * @return handle to {@link android.hardware.Camera} that was opened
	 */
	public static Camera open() {

		// for gingerbread use the the specific method
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
			return OpenCameraInterface.openGingerBread();
		}

		// otherwise try with this older version
		Log.i(TAG, "Opening camera in pre gingerbread era");
		Camera camera = Camera.open();

		return camera;
	}

	/**
	 * a more advanced version that can be used from gingerbread and up
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static Camera openGingerBread() {
		int numCameras = Camera.getNumberOfCameras();
		if (numCameras == 0) {
			Log.w(TAG, "No cameras!");
			return null;
		}

		int index = 0;
		while (index < numCameras) {
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			Camera.getCameraInfo(index, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				break;
			}
			index++;
		}

		Camera camera;
		if (index < numCameras) {
			Log.i(TAG, "Opening camera #" + index);
			camera = Camera.open(index);
		} else {
			Log.i(TAG, "No camera facing back; returning camera #0");
			camera = Camera.open(0);
		}

		return camera;
	}

}
