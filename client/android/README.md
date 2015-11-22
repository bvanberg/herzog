# CameraPreviewSample

The original project demonstrates how to implement a camera preview in android step-by-step.

**Update: This fork focuses more on layout, code quality and graphical improvements.
The support actionbar was added, icon updated and several code and layout changes introduced. 
Latest changes add barcode recognition using the zxing core library.**

## branches

Most work is done on the development branch. From time to time we will create feature branch that
will some day end up in the development branch anyway.

The original project had several branches to explain step by step how the preview is created. We
left these intact.

## zxing library integration and API support

This example app supports back to API level 7. Since this is an Android Studio project the API level is defined in the app/build.gradle file: 

    defaultConfig {
        minSdkVersion 7
        targetSdkVersion 19
    }
    
The zxing library needed some work to get support back to this level. Some of the changes we've done are listed below. 

### SDK version

Android build number can be checked in code like this

    if( android.os.Build.VERSION.SDK_INT < android.os.Build.VERSIONS.HONEYCOMB )
        // do something here we should only do for devices below honeycomb

### camera preview failed RuntimeException

    SurfaceView surface=(SurfaceView)findViewById(R.id.surfaceView1);
    SurfaceHolder holder=surface.getHolder();
    holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // this does the magic!
    
### get screen size

Retrieving device screen size is typically needed to determine the camera preview size. The method
calls to do so have changed of different API revisions. This is how to catch that.
    
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
			display.getSize(theScreenResolution);
	} else {
			theScreenResolution.x = display.getWidth();
			theScreenResolution.y = display.getHeight();
	}

## Version History

### v1.3.0 (upcoming)

* general code improvements where needed (check tags)

### v1.2.0

* zxing added
* improved documentation on how to handle api levels

### v1.1.0 

first tagged version with the improvements on this fork

### Upcoming features

Features are developed on so called feature branches.

* see if we really need a wrapping view, nested views are no good for performance anyway
* work on extra camera parameters like focus
* implement actual image capture and bitmap handling

### Known issues

Fixes for these issues are currently in progress on the development branch and will be integrated
once tested.

* the resizing always happens within the previous dimensions so keeps getting smaller

### Fixed

* alternative camera selection broken on 2.3.6 => was related to a fixed number of camera's instead of a number based on the available camera's.
* SampleActivity does no longer center the preview => CENTER_IN_PARENT requires RelativeLayout, that example had still a RelativeLayout anyway so adapted.
* more advanced hardware checks in code => already in place

## Resources

### External links

* [Android Training Article about Camera control](http://developer.android.com/training/camera/index.html)
* [Android training about adding Actionbar](https://developer.android.com/training/basics/actionbar/index.html)
* [Android Camera API Tutorial](http://www.vogella.com/tutorials/AndroidCamera/article.html) by Lars Vogel

## Original Readme

The below overview of tags is part of the original project showing step by step how several aspects
of managing the camera and its preview are handled.

The biggest concern in my opinion was that this code relies on the wrapping layout ro be a
RelativeLayout instance and if not simply breaks. In my version this was set to a more generic
ViewGroup object. Other than that it's a great way of showing how things are done.

## Git Tags

* minimum -- Minimum code implementing camera preview.
* orientation -- Display in correct orientation.
* aspectratio -- Display in correct aspect ratio.
* aspectratio_refine -- Fixed a non-critical issue that onSurfaceChanged() is called twice.
* resizable -- Includes UI to arbitrarily set camera preview size.
* previewsize_fix -- Fixed a problem that preview couldn't start even with supported preview size.
* multicamera -- Supports multiple cameras.
* practical -- Allows the user program to set the layout size, position, and callbacks.

### minimum

This release is a minimum implementation of camera preview, which may display the preview in wrong orientation and broken aspect ratio.

### orientation

This release handles the orientation change to display in the correct orientation.

### aspectratio

This release displays camera preview in correct aspect ratio.
Whatever the actual size of the preview is, it keeps the width & height ratio while it scale the preview to fit to the display.
This although has a non-critical issue that might configure the camera parameters twice for one configuration change of the display.
It is caused by the multiple invocation of onSurfaceChanged for one orientation change because layout parameter might be updated at the first call of onSurfaceChanged.
This will be fixed in the next release.

### aspectratio_refine

This release fixed the known issue explained in the previous section.
It was also changed the algorithm to select the preview size, and now picks the size of which the ratio is closest to the one of the display.
There is a known issue that the camera preview might fail to start even with the supported preview size on some devices.
It is observed on Japanese device, IS03, so far.

### resizable

This release allows user to arbitrarily set the size of camera preview from the supported sizes.
It still keeps the aspect ratio and scales it to fit to the display.

#### Files

* CameraPreview.java -- Primary file implementing camera preview feature.
* MainActivity.java -- Driver Activity to demonstrate the usage of CameraPreview.java.
    This file includes both code that utilizes CameraPreview and ResizeableCameraPreview, one of them commented out.
* ResizableCameraPreview.java -- Extended class of CameraPreview allowing user to specify the preview size.

### picturesize_fix

This release includes a fix that solves the issue that the preview can't be started even with supported preview size.
The solution was to set picture size with supported picture size, as well as preview size.
This release also supported more display rotation, Surface.ROTATION_270 in addition to Surface.ROTATION_90. This affects only Android 2.2 and later.

#### Known Issues

* If the display rotation is changed from Surface.ROTATION_90 to Surface.ROTATION_270 or vice versa, the rotation of the camera preview is not updated.
* Orientation is not correct on an optimus pad (or all Honeycomb).

### multicamera

This release supports multiple camera.
If the device has more than one camera on it, the user can choose a camera to display the camera preview.
CameraPreview (and ResizableCameraPreview) takes a camera ID, which is usually 0 for back-facing camera and 1 for front-facing camera if exists.
To switch between cameras, call stop() and instantiate another camera preview object with different camera ID.
This release also shows how to stop the camera preview when it is not on the foreground.
Stopping camera preview prevents from wasting the battery power and blocking other applications to use camera device.

### practical

This version allows user class to specify the size and position of the camera preview.
Also, implemented setter for a callback called on preview-ready, and wrapper for Camera.set*Callback.
Refined sample driver activities.

#### Files

* CameraPreview.java -- Primary file implementing camera preview feature.
* MainActivity.java -- UI that allows users to select a sample activity or a test activity to open.
* CameraPreviewSampleActivity.java -- Driver Activity to demonstrate the usage of CameraPreview.java.
* CameraPreviewTestActivity.java -- Driver Activity for testing CameraPreview.java.
* ResizableCameraPreview.java -- Extended class of CameraPreview allowing user to specify the preview size, for the purpose of testing.
