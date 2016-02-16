package com.herzog.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Sample driver class to demonstrate the use of CameraPreview class. This contains UI controls
 * to start the several preview options.
 */
public class MainActivity extends ActionBarActivity implements View.OnClickListener {

	private final String WS_BASE = "http://photoid-env.elasticbeanstalk.com";
	private final String PHOTO_URL_ENDPOINT = "/identification/photo/url";

	/**
	 * request code for image capture to check on handling intent result
	 */
	private static final int REQUEST_IMAGE_CAPTURE = 1;

	/**
	 * the only bitmap so we can keep track of it and recycle if needed
	 */
	private Bitmap mBitmap = null;

	/**
	 * an imageview to show the taken picture
	 */
	private ImageView mImageView = null;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.main);

//        findViewById(R.id.button_sample).setOnClickListener(this);
//        findViewById(R.id.button_test).setOnClickListener(this);
		findViewById(R.id.button_intent).setOnClickListener(this);
//        findViewById(R.id.button_zxing).setOnClickListener(this);

//		mImageView = (ImageView) findViewById(R.id.imageView);
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
//            case R.id.button_sample:
//                intent = new Intent(this, CameraPreviewSampleActivity.class);
//                startActivity(intent);
//                break;
//            case R.id.button_test:
//                intent = new Intent(this, CameraPreviewTestActivity.class);
//                startActivity(intent);
//                break;
			case R.id.button_intent:
				dispatchTakePictureIntent();
				break;
//            case R.id.button_zxing:
//                Intent i = new Intent(this, CaptureActivity.class);
//                i.putExtra(CaptureActivity.KEY_INFO_TEXT, "some info text goes here");// TODO translate
//                // TODO we should use activity for result
//                //i.putExtra(CaptureActivity.KEY_RESULT_HANDLER, new ResultHandler(){
//                //    @Override
//                //    public void handleResult(Bundle data) {
//                //        // TODO show data instead
//                //    }
//                //});
//                startActivity(i);
//
//                break;
		}
	}

	/**
	 * helper to launch the capture intent
	 */
	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// here we get back the thumbnail of the taken picture
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			// Since we used an intent the default camera app (user preferences) is used and
			// the image is stored in gallery
			mBitmap = (Bitmap) extras.get("data");

			// this sets the captured image to image view to view a thumbnail snap of it.
			//mImageView.setImageBitmap(mBitmap);

			if (mBitmap != null) {

				OutputStream os = null;
				final File photo = new File(this.getCacheDir(), "tmpPhoto");
				try {

					os = new BufferedOutputStream(new FileOutputStream(photo));
					mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);

					new UploadTask().execute(photo);

				} catch (final Exception ex) {
					Log.e("JsonClient.uploadFile", ex.getMessage(), ex);
					Toast.makeText(
							getApplicationContext(),
							"Upload Error: " + ex.getMessage(),
							Toast.LENGTH_LONG).show();
				} finally {
					if (os != null) try { os.close(); } catch (IOException io) {}
				}

			}
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// TODO check if this is sufficient for clean up.
		// TODO: Older Android versions do have an issue with memory handling related to bitmaps
		if (mBitmap != null && !mBitmap.isRecycled())
			mBitmap.recycle();
		mBitmap = null;
	}


	class UploadTask extends AsyncTask<File, Void, HttpResponse> {

		private Throwable throwable;
		private File photo = null;
		final ResponseHandler<String> handler = new BasicResponseHandler();

		protected HttpResponse doInBackground(File... files) {

			HttpResponse response;
			OutputStream os = null;
			InputStream is = null;

			photo = files[0];

			try {
				final HttpClient httpclient = new DefaultHttpClient();
				final HttpGet get = new HttpGet(WS_BASE + PHOTO_URL_ENDPOINT);

				response = httpclient.execute(get);

				final String preSignedUrl = handler.handleResponse(response);

				final URL preSignedUrlForUpload = new URL(preSignedUrl);
				final HttpURLConnection connection = (HttpURLConnection) preSignedUrlForUpload.openConnection();
				connection.setDoOutput(true);
				connection.setRequestMethod("PUT");
				connection.setRequestProperty("Content-Type", "binary/octet-stream");
				is = new FileInputStream(photo);
				os = connection.getOutputStream();

				final int bytesCopied = IOUtils.copy(is, os);
				Log.i("UploadTask - ", "Photo File Size: " + (bytesCopied / 1024) + "KB");

				os.flush();

				int responseCode = connection.getResponseCode();
				Log.i("UploadTask - ", "AWS S3 Upload Response: " + responseCode);

			} catch (final Throwable e) {
				// show error
				Log.e("JsonClient.uploadFile", e.getMessage(), e);
				this.throwable = e;
				response = null;
			} finally {
				IOUtils.closeQuietly(is);
				IOUtils.closeQuietly(os);
			}

			return response;

		}

		protected void onPostExecute(HttpResponse response) {
			// TODO: check this.exception

			Toast.makeText(
					getApplicationContext(),
					"Http Response Code: " + response.getStatusLine().getStatusCode(),
					Toast.LENGTH_LONG).show();

			// clean up photo and original bitmap
			if (photo != null && photo.exists()) {
				Log.i("onPostExecute - ", "Deleted photo: " + photo.delete());
			}

			if (mBitmap != null && !mBitmap.isRecycled()) {
				mBitmap.recycle();
			}

			mBitmap = null;

		}

	}

}
