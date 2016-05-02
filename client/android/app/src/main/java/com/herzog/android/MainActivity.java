package com.herzog.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.herzog.android.bean.ItemUrl;
import com.herzog.android.bean.PhotoCapture;
import com.herzog.android.bean.PhotoMetadata;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample driver class to demonstrate the use of CameraPreview class. This contains UI controls
 * to start the several preview options.
 */
public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private final String WS_BASE = "http://photoid-env.elasticbeanstalk.com";
    private final String PHOTO_URL_ENDPOINT = "/identification/photo/url";
    private final String META_DATA_URL_ENDPOINT = "/identification/photo/metadata";

    final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * request code for image capture to check on handling intent result
     */
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    /**
     * the only bitmap so we can keep track of it and recycle if needed
     */
    private Bitmap mBitmap = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main);
        findViewById(R.id.button_intent).setOnClickListener(this);
   }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.button_intent:
                dispatchTakePictureIntent();
                break;
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

            final Bundle extras = data.getExtras();

            // Since we used an intent the default camera app (user preferences) is used and
            // the image is stored in gallery
            mBitmap = (Bitmap) extras.get("data");

            if (mBitmap != null) {

                OutputStream os = null;
                final File photo = new File(this.getCacheDir(), "tmpPhoto");
                try {

                    os = new BufferedOutputStream(new FileOutputStream(photo));
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);

                    // prompt user with dialog so they can enter a description
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Photo Description");

                    // Set up the input
                    final EditText input = new EditText(this);

                    // Specify the type of input expected; multi-line input text
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String description = input.getText().toString();
                            new SubmitPhotoTask().execute(new PhotoCapture(photo, description));
                        }
                    });

                    builder.show();

                } catch (final Exception ex) {
                    Log.e("JsonClient.uploadFile", ex.getMessage(), ex);
                    Toast.makeText(
                            getApplicationContext(),
                            "Upload Error: " + ex.getMessage(),
                            Toast.LENGTH_LONG).show();
                } finally {
                    if (os != null) try {
                        os.close();
                    } catch (IOException io) {
                    }
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


    class SubmitPhotoTask extends AsyncTask<PhotoCapture, Void, HttpResponse> {

        File photo = null;

        HttpResponse response;

        protected HttpResponse doInBackground(PhotoCapture... photoCapture) {

            final PhotoCapture capture = photoCapture[0];

            // grab the photo to upload to S3 via presigned url
            photo = capture.getPhoto();
            final String description = capture.getDescription();

            final String s3Key = upload(photo);

            if (s3Key != null) {
                response = postMetadata(s3Key, description);
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

    private String upload(final File photo) {

        String s3KeyToReturn = null;
        OutputStream os = null;
        InputStream is = null;

        try {
            final HttpClient httpclient = new DefaultHttpClient();
            final ResponseHandler<String> handler = new BasicResponseHandler();
            final HttpGet get = new HttpGet(WS_BASE + PHOTO_URL_ENDPOINT);

            HttpResponse response = httpclient.execute(get);

            final ItemUrl itemUrl = objectMapper.readValue(handler.handleResponse(response), ItemUrl.class);
            final URL preSignedUrlForUpload = new URL(itemUrl.getPresignedUrl());
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

            if (response.getStatusLine().getStatusCode() != 200) {
                Log.e(
                    MainActivity.class.getName(),
                    "S3 Upload failed, http response code was " + response.getStatusLine().getStatusCode()
                );
            } else {
                s3KeyToReturn = itemUrl.getKey();
            }

        } catch (final Throwable e) {
            // show error
            Log.e("JsonClient.uploadFile", e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }

        return s3KeyToReturn;
    }


    private HttpResponse postMetadata(final String s3Key, final String description) {

        HttpResponse response = null;

        try {

            final HttpClient httpclient = new DefaultHttpClient();
            HttpPost post = new HttpPost(WS_BASE + META_DATA_URL_ENDPOINT);

            Map<String, String> metadata = new HashMap<String, String>();
            metadata.put("description", description);

            // todo: make these real values
            final PhotoMetadata photoMetadata = new PhotoMetadata();
            photoMetadata.setUserId("some_unique_user_id");
            photoMetadata.setMetadata(metadata);
            photoMetadata.setPhotoKeys(Arrays.asList(s3Key));

            final String meta = objectMapper.writeValueAsString(photoMetadata);
            Log.i(MainActivity.class.getName(), meta);
            final StringEntity reqEntity = new StringEntity(meta);
            reqEntity.setContentType("application/json");
            post.setEntity(reqEntity);
            response = httpclient.execute(post);

        } catch (Exception e) {
            // show error
            Log.e("failed to post metadata", e.getMessage(), e);
        }

        return response;
    }


}
