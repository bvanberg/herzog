package com.herzog.android.zxing.camera;

import java.util.Collections;
import java.util.List;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
// FIXME not in use, if used make it post ICS only
public final class MeteringInterface {

  private static final String TAG = MeteringInterface.class.getSimpleName();
  private static final int AREA_PER_1000 = 400;

  private MeteringInterface() {
  }

  public static void setFocusArea(Camera.Parameters parameters) {
    if (parameters.getMaxNumFocusAreas() > 0) {
      Log.i(TAG, "Old focus areas: " + toString(parameters.getFocusAreas()));
      List<Camera.Area> middleArea = buildMiddleArea();
      Log.i(TAG, "Setting focus area to : " + toString(middleArea));
      parameters.setFocusAreas(middleArea);
    } else {
      Log.i(TAG, "Device does not support focus areas");
    }
  }

  public static void setMetering(Camera.Parameters parameters) {
    if (parameters.getMaxNumMeteringAreas() > 0) {
      Log.i(TAG, "Old metering areas: " + parameters.getMeteringAreas());
      List<Camera.Area> middleArea = buildMiddleArea();
      Log.i(TAG, "Setting metering area to : " + toString(middleArea));
      parameters.setMeteringAreas(middleArea);
    } else {
      Log.i(TAG, "Device does not support metering areas");
    }
  }

  private static List<Camera.Area> buildMiddleArea() {
    return Collections.singletonList(
        new Camera.Area(new Rect(-AREA_PER_1000, -AREA_PER_1000, AREA_PER_1000, AREA_PER_1000), 1));
  }

  private static String toString(Iterable<Camera.Area> areas) {
    if (areas == null) {
      return null;
    }
    StringBuilder result = new StringBuilder();
    for (Camera.Area area : areas) {
      result.append(area.rect).append(':').append(area.weight).append(' ');
    }
    return result.toString();
  }

}
