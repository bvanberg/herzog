package com.herzog.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import android.widget.Toast;
import com.herzog.android.zxing.CaptureActivity;
import com.herzog.android.zxing.ResultHandler;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;

/**
 * Sample driver class to demonstrate the use of CameraPreview class. This contains UI controls
 * to start the several preview options.
 */
public class MainActivity extends ActionBarActivity implements View.OnClickListener {

	private final String WS_BASE = "http://172.16.1.16:8080";
	private final String IDENTIFICATION_ENDPOINT = "/identification";

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
		// TODO check if this is sufficient for clean up. Older Android versions do have an issue with memory handling related to bitmaps
		if (mBitmap != null && !mBitmap.isRecycled())
			mBitmap.recycle();
		mBitmap = null;
	}


	class UploadTask extends AsyncTask<File, Void, HttpResponse> {

		private Exception exception;
		private File photo = null;

		protected HttpResponse doInBackground(File... files) {

			HttpResponse response = null;

			photo = files[0];

			try {
				final HttpClient httpclient = new DefaultHttpClient();

				HttpPost post = new HttpPost(WS_BASE + IDENTIFICATION_ENDPOINT);

				final InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(photo), -1);
				reqEntity.setContentType("binary/octet-stream");
				reqEntity.setChunked(true); // Send in multiple parts if needed
				post.setEntity(reqEntity);
				response = httpclient.execute(post);

			} catch (Exception e) {
				// show error
				Log.e("JsonClient.uploadFile", e.getMessage(), e);
				this.exception = e;
				return null;
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
