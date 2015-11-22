package com.herzog.android.zxing;

import android.graphics.BitmapFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultPointCallback;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.herzog.android.R;

import java.util.Collection;
import java.util.Map;

/**
 * This class handles all the messaging which comprises the state machine for
 * fragment_capture.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler {

	// moved to mediator
	// private final CaptureActivity activity;
	// private final CameraManager cameraManager;

	private final DecodeThread decodeThread;
	private State state;

	private enum State {
		PREVIEW, SUCCESS, DONE
	}

	CaptureActivityHandler(Collection<BarcodeFormat> decodeFormats,
			Map<DecodeHintType, ?> baseHints, String characterSet,
			ResultPointCallback resultPointCallback) {
		decodeThread = new DecodeThread(decodeFormats, baseHints, characterSet,
				resultPointCallback);
		decodeThread.start();
		state = State.SUCCESS;

		// Start ourselves capturing previews and decoding.
		Mediator.getInstance().getCameraManager().startPreview();
		restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {
		if (message.what == R.id.restart_preview) {
			restartPreviewAndDecode();
		} else if (message.what == R.id.decode_succeeded) {
			state = State.SUCCESS;
			Bundle bundle = message.getData();
			Bitmap barcode = null;
			float scaleFactor = 1.0f;
			if (bundle != null) {
				byte[] compressedBitmap = bundle
						.getByteArray(DecodeThread.BARCODE_BITMAP);
				if (compressedBitmap != null) {
					barcode = BitmapFactory.decodeByteArray(compressedBitmap,
							0, compressedBitmap.length, null);
					// Mutable copy:
					barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
				}
				scaleFactor = bundle
						.getFloat(DecodeThread.BARCODE_SCALED_FACTOR);
			}
			Mediator.getInstance().getCaptureActivity()
					.handleDecode((Result) message.obj, barcode, scaleFactor);
		} else if (message.what == R.id.decode_failed) {
			// We're decoding as fast as possible, so when one decode fails,
			// start another.
			state = State.PREVIEW;
			Mediator.getInstance()
					.getCameraManager()
					.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
		}
	}

	public void quitSynchronously() {
		state = State.DONE;
		Mediator.getInstance().getCameraManager().stopPreview();
		Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
		quit.sendToTarget();
		try {
			// Wait at most half a second; should be enough time, and onPause()
			// will timeout quickly
			decodeThread.join(500L);
		} catch (InterruptedException e) {
			// continue
		}

		// Be absolutely sure we don't send any queued up messages
		removeMessages(R.id.decode_succeeded);
		removeMessages(R.id.decode_failed);
	}

	private void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			Mediator.getInstance()
					.getCameraManager()
					.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
			Mediator.getInstance().getCaptureActivity().drawViewfinder();
		}
	}

}
