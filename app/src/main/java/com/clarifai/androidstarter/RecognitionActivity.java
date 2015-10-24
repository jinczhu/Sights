package com.clarifai.androidstarter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;
import com.clarifai.api.exception.ClarifaiException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecognitionActivity extends Activity {
  private static final String TAG = RecognitionActivity.class.getSimpleName();
  private static final String APP_ID = "fGtUHi8JOvqPrpgI1UDZisTBJ7qDOMo4XFCNHjGc";
  private static final String APP_SECRET = "iyO7xtPq_htZnItjPCXoEkYR6tFCao5hwXHCf795";

  private static final int MEDIA_TYPE_IMAGE = 1;

  private final ClarifaiClient client = new ClarifaiClient(APP_ID, APP_SECRET);
  private Button selectButton;
  private TextView textView;
  private Camera camera;
  private CSView camView;
  private FrameLayout prev;

  private Uri imguri;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_recognition);

    textView = (TextView) findViewById(R.id.text_view);
    selectButton = (Button) findViewById(R.id.select_button);
    camera = getCameraInstance();
    camView = new CSView(this, camera);
    prev = (FrameLayout) findViewById(R.id.cam_view);
    prev.addView(camView);

    selectButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        camView.capture(mCamera);

      }
    });
  }

  public static Camera getCameraInstance(){
    Camera c = null;
    try {
      c = Camera.open(); // attempt to get a Camera instance
    }
    catch (Exception e){
      // Camera is not available (in use or does not exist)
    }
    return c; // returns null if camera is unavailable
  }

  private Camera.PictureCallback mCamera = new Camera.PictureCallback() {

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

      File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
      imguri = Uri.fromFile(getOutputMediaFile(MEDIA_TYPE_IMAGE));
      if (pictureFile == null){
        Log.d(TAG, "Error creating media file, check storage permissions");
        return;
      }

      try {
        FileOutputStream fos = new FileOutputStream(pictureFile);
        fos.write(data);
        fos.close();
      } catch (FileNotFoundException e) {
        Log.d(TAG, "File not found: " + e.getMessage());
      } catch (IOException e) {
        Log.d(TAG, "Error accessing file: " + e.getMessage());
      }

      Log.d(TAG, "User picked image: " + imguri);
      Bitmap bitmap = loadBitmapFromUri(imguri);
      if (bitmap != null) {
        textView.setText("Recognizing...");
        selectButton.setEnabled(false);

        // Run recognition on a background thread since it makes a network call.
        new AsyncTask<Bitmap, Void, RecognitionResult>() {
          @Override protected RecognitionResult doInBackground(Bitmap... bitmaps) {
            return recognizeBitmap(bitmaps[0]);
          }
          @Override protected void onPostExecute(RecognitionResult result) {
            updateUIForResult(result);
          }
        }.execute(bitmap);
      } else {
        textView.setText("Unable to load selected image.");
      }

    }
  };

  private static File getOutputMediaFile(int type){
    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), "Sights");

    // Create the storage directory if it does not exist
    if (! mediaStorageDir.exists()){
      if (! mediaStorageDir.mkdirs()){
        Log.d("Sights", "failed to create directory");
        return null;
      }
    }

    // Create a media file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    File mediaFile;
    if (type == MEDIA_TYPE_IMAGE){
      mediaFile = new File(mediaStorageDir.getPath() + File.separator +
              "IMG_"+ timeStamp + ".jpg");
    } else {
      return null;
    }

    return mediaFile;
  }

  /** Create a file Uri for saving an image or video */
  private static Uri getOutputMediaFileUri(int type){
    return Uri.fromFile(getOutputMediaFile(type));
  }

  /** Loads a Bitmap from a content URI returned by the media picker. */
  private Bitmap loadBitmapFromUri(Uri uri) {
    try {
      // The image may be large. Load an image that is sized for display. This follows best
      // practices from http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
      BitmapFactory.Options opts = new BitmapFactory.Options();
      opts.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, opts);
      int sampleSize = 1;
      while (opts.outWidth / (2 * sampleSize) >= prev.getWidth() &&
             opts.outHeight / (2 * sampleSize) >= prev.getHeight()) {
        sampleSize *= 2;
      }

      opts = new BitmapFactory.Options();
      opts.inSampleSize = sampleSize;
      return BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, opts);
    } catch (IOException e) {
      Log.e(TAG, "Error loading image: " + uri, e);
    }
    return null;
  }

  /** Sends the given bitmap to Clarifai for recognition and returns the result. */
  private RecognitionResult recognizeBitmap(Bitmap bitmap) {
    try {
      // Scale down the image. This step is optional. However, sending large images over the
      // network is slow and  does not significantly improve recognition performance.
      Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 320,
          320 * bitmap.getHeight() / bitmap.getWidth(), true);

      // Compress the image as a JPEG.
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      scaled.compress(Bitmap.CompressFormat.JPEG, 90, out);
      byte[] jpeg = out.toByteArray();

      // Send the JPEG to Clarifai and return the result.
      return client.recognize(new RecognitionRequest(jpeg)).get(0);
    } catch (ClarifaiException e) {
      Log.e(TAG, "Clarifai error", e);
      return null;
    }
  }

  /** Updates the UI by displaying tags for the given result. */
  private void updateUIForResult(RecognitionResult result) {
    if (result != null) {
      if (result.getStatusCode() == RecognitionResult.StatusCode.OK) {
        // Display the list of tags in the UI.
        StringBuilder b = new StringBuilder();
        for (Tag tag : result.getTags()) {
          b.append(b.length() > 0 ? ", " : "").append(tag.getName());
        }
        textView.setText("Tags:\n" + b);
      } else {
        Log.e(TAG, "Clarifai: " + result.getStatusMessage());
        textView.setText("Sorry, there was an error recognizing your image.");
      }
    } else {
      textView.setText("Sorry, there was an error recognizing your image.");
    }
    selectButton.setEnabled(true);
  }
}
