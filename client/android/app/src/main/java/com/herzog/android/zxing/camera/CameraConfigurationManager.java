package com.herzog.android.zxing.camera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

/**
 * A class which deals with reading, parsing, and setting the camera parameters
 * which are used to configure the camera hardware.
 */

public class CameraConfigurationManager {

	private static final String TAG = "CameraConfiguration";

	// This is bigger than the size of a small screen, which is still supported.
	// The routine
	// below will still select the default (presumably 320x240) size for these.
	// This prevents
	// accidental selection of very low resolution on some devices.
	private static final int MIN_PREVIEW_PIXELS = 480 * 320; // normal screen
	private static final double MAX_ASPECT_DISTORTION = 0.15;
	private static final int MIN_FPS = 5;

	private final Context context;
	private Point viewResolution;
	private Point cameraResolution;

	public CameraConfigurationManager(Context context) {
		this.context = context;
	}

	/**
	 * Reads, one time, values from the camera that are needed by the app.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void initFromCameraParameters(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		Point theScreenResolution = new Point();
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2)
			display.getSize(theScreenResolution);
		else {
			theScreenResolution.x = display.getWidth();
			theScreenResolution.y = display.getHeight();
		}
		viewResolution = theScreenResolution;
		// Log.i(TAG, "Screen resolution: " + screenResolution);

		// FIXME use view resolution instead here
		cameraResolution = findBestPreviewSizeValue(parameters, viewResolution);
		Log.i(TAG, "Camera resolution: " + cameraResolution);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint({ "InlinedApi", "NewApi" })
	public void setDesiredCameraParameters(Camera camera, boolean safeMode) {
		Camera.Parameters parameters = camera.getParameters();

		if (parameters == null) {
			Log.w(
					TAG,
					"Device error: no camera parameters are available. Proceeding without configuration.");
			return;
		}

		Log.i(TAG, "Initial camera parameters: " + parameters.flatten());

		if (safeMode) {
			Log.w(TAG,
					"In camera config safe mode -- most settings will not be honored");
		}

		// removed prefs, use best available
		// SharedPreferences prefs =
		// PreferenceManager.getDefaultSharedPreferences(context);

		// initializeTorch(parameters, prefs, safeMode);

		setBestPreviewFPS(parameters);

		String focusMode = null;
		// if (prefs.getBoolean(PreferencesActivity.KEY_AUTO_FOCUS, true)) {
		if (safeMode) {
			// ||
			// prefs.getBoolean(PreferencesActivity.KEY_DISABLE_CONTINUOUS_FOCUS,
			// true)) {
			focusMode = findSettableValue(parameters.getSupportedFocusModes(),
					Camera.Parameters.FOCUS_MODE_AUTO);
		} else {
			// try with autofocus first
			//if( android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH )
				focusMode = findSettableValue(parameters.getSupportedFocusModes(),
						Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
						Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
						Camera.Parameters.FOCUS_MODE_AUTO);
		}
		// }
		// Maybe selected auto-focus but not available, so fall through here:
		if (!safeMode && focusMode == null) {
			focusMode = findSettableValue(parameters.getSupportedFocusModes(),
					Camera.Parameters.FOCUS_MODE_MACRO,
					Camera.Parameters.FOCUS_MODE_EDOF);
		}
		if (focusMode != null) {
			parameters.setFocusMode(focusMode);
		}

		// set rotation
		// FIXME rotation fails now, image is not properly rotated
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		int rotation = 0;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO)
			rotation = manager.getDefaultDisplay().getRotation();
		else
			rotation = manager.getDefaultDisplay().getOrientation();
		int angle = 0;
		switch (rotation) {
		case Surface.ROTATION_0: // This is display orientation
			angle = 90; // This is camera orientation
			mRotated = true;
			break;
		case Surface.ROTATION_90:
			angle = 0;
			break;
		case Surface.ROTATION_180:
			angle = 270; // should we support these
			break;
		case Surface.ROTATION_270:
			angle = 180; // should we support these
			break;
		default:
			angle = 90;
			mRotated = true;
			break;
		}

		parameters.setRotation(angle);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO)
			camera.setDisplayOrientation(angle);

		parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
		camera.setParameters(parameters);

		Camera.Parameters afterParameters = camera.getParameters();
		Camera.Size afterSize = afterParameters.getPreviewSize();
		if (afterSize != null
				&& (cameraResolution.x != afterSize.width || cameraResolution.y != afterSize.height)) {
			Log.w(TAG, "Camera said it supported preview size "
					+ cameraResolution.x + 'x' + cameraResolution.y
					+ ", but after setting it, preview size is "
					+ afterSize.width + 'x' + afterSize.height);
			cameraResolution.x = afterSize.width;
			cameraResolution.y = afterSize.height;
		}
	}

	private boolean mRotated = false;

	public boolean isRotated() {
		return mRotated;
	}

	public Point getCameraResolution() {
		return cameraResolution;
	}

	public Point getViewResolution() {
		return viewResolution;
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	private void doSetTorch(Camera.Parameters parameters, boolean newSetting,
			boolean safeMode) {
		String flashMode;
		if (newSetting) {
			flashMode = findSettableValue(parameters.getSupportedFlashModes(),
					Camera.Parameters.FLASH_MODE_TORCH,
					Camera.Parameters.FLASH_MODE_ON);
		} else {
			flashMode = findSettableValue(parameters.getSupportedFlashModes(),
					Camera.Parameters.FLASH_MODE_OFF);
		}
		if (flashMode != null) {
			parameters.setFlashMode(flashMode);
		}

	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static void setBestPreviewFPS(Camera.Parameters parameters) {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD) {
			return;
		}
		// Required for Glass compatibility; also improves battery/CPU
		// performance a tad
		List<int[]> supportedPreviewFpsRanges = parameters
				.getSupportedPreviewFpsRange();
		Log.i(TAG, "Supported FPS ranges: "
				+ toString(supportedPreviewFpsRanges));
		if (supportedPreviewFpsRanges != null
				&& !supportedPreviewFpsRanges.isEmpty()) {
			int[] minimumSuitableFpsRange = null;
			for (int[] fpsRange : supportedPreviewFpsRanges) {
				int fpsMax = fpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
				if (fpsMax >= MIN_FPS * 1000
						&& (minimumSuitableFpsRange == null || fpsMax > minimumSuitableFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX])) {
					minimumSuitableFpsRange = fpsRange;
				}
			}
			if (minimumSuitableFpsRange == null) {
				Log.i(TAG, "No suitable FPS range?");
			} else {
				int[] currentFpsRange = new int[2];
				parameters.getPreviewFpsRange(currentFpsRange);
				if (!Arrays.equals(currentFpsRange, minimumSuitableFpsRange)) {
					Log.i(
							TAG,
							"Setting FPS range to "
									+ Arrays.toString(minimumSuitableFpsRange));
					parameters
							.setPreviewFpsRange(
									minimumSuitableFpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
									minimumSuitableFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
				}
			}
		}
	}

	// Actually prints the arrays properly:
	private static String toString(Collection<int[]> arrays) {
		if (arrays == null || arrays.isEmpty()) {
			return "[]";
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append('[');
		Iterator<int[]> it = arrays.iterator();
		while (it.hasNext()) {
			buffer.append(Arrays.toString(it.next()));
			if (it.hasNext()) {
				buffer.append(", ");
			}
		}
		buffer.append(']');
		return buffer.toString();
	}

	private Point findBestPreviewSizeValue(Camera.Parameters parameters,
			Point screenResolution) {

		List<Camera.Size> rawSupportedSizes = parameters
				.getSupportedPreviewSizes();
		if (rawSupportedSizes == null) {
			Log.w(TAG,
					"Device returned no supported preview sizes; using default");
			Camera.Size defaultSize = parameters.getPreviewSize();
			return new Point(defaultSize.width, defaultSize.height);
		}

		// Sort by size, descending
		List<Camera.Size> supportedPreviewSizes = new ArrayList<Camera.Size>(
				rawSupportedSizes);
		Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
			@Override
			public int compare(Camera.Size a, Camera.Size b) {
				int aPixels = a.height * a.width;
				int bPixels = b.height * b.width;
				if (bPixels < aPixels) {
					return -1;
				}
				if (bPixels > aPixels) {
					return 1;
				}
				return 0;
			}
		});

		// if (Log.isLoggable(TAG, Log.INFO)) {
		// StringBuilder previewSizesString = new StringBuilder();
		// for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
		// previewSizesString.append(supportedPreviewSize.width).append('x')
		// .append(supportedPreviewSize.height).append(' ');
		// }
		// Log.i(TAG, "Supported preview sizes: " + previewSizesString);
		// }

		double screenAspectRatio = (double) screenResolution.x
				/ (double) screenResolution.y;

		// Remove sizes that are unsuitable
		Iterator<Camera.Size> it = supportedPreviewSizes.iterator();
		while (it.hasNext()) {
			Camera.Size supportedPreviewSize = it.next();
			int realWidth = supportedPreviewSize.width;
			int realHeight = supportedPreviewSize.height;
			if (realWidth * realHeight < MIN_PREVIEW_PIXELS) {
				it.remove();
				continue;
			}

			boolean isCandidatePortrait = realWidth < realHeight;
			int maybeFlippedWidth = isCandidatePortrait ? realHeight
					: realWidth;
			int maybeFlippedHeight = isCandidatePortrait ? realWidth
					: realHeight;
			double aspectRatio = (double) maybeFlippedWidth
					/ (double) maybeFlippedHeight;
			double distortion = Math.abs(aspectRatio - screenAspectRatio);
			if (distortion > MAX_ASPECT_DISTORTION) {
				it.remove();
				continue;
			}

			if (maybeFlippedWidth == screenResolution.x
					&& maybeFlippedHeight == screenResolution.y) {
				Point exactPoint = new Point(realWidth, realHeight);
				Log.i(TAG,
						"Found preview size exactly matching screen size: "
								+ exactPoint);
				return exactPoint;
			}
		}

		// If no exact match, use largest preview size. This was not a great
		// idea on older devices because
		// of the additional computation needed. We're likely to get here on
		// newer Android 4+ devices, where
		// the CPU is much more powerful.
		if (!supportedPreviewSizes.isEmpty()) {
			Camera.Size largestPreview = supportedPreviewSizes.get(0);
			Point largestSize = new Point(largestPreview.width,
					largestPreview.height);
			Log.i(TAG, "Using largest suitable preview size: " + largestSize);
			return largestSize;
		}

		// If there is nothing at all suitable, return current preview size
		Camera.Size defaultPreview = parameters.getPreviewSize();
		Point defaultSize = new Point(defaultPreview.width,
				defaultPreview.height);
		Log.i(TAG, "No suitable preview sizes, using default: "
				+ defaultSize);
		return defaultSize;
	}

	private static String findSettableValue(Collection<String> supportedValues,
			String... desiredValues) {
		Log.i(TAG, "Supported values: " + supportedValues);
		String result = null;
		if (supportedValues != null) {
			for (String desiredValue : desiredValues) {
				if (supportedValues.contains(desiredValue)) {
					result = desiredValue;
					break;
				}
			}
		}
		Log.i(TAG, "Settable value: " + result);
		return result;
	}

}
