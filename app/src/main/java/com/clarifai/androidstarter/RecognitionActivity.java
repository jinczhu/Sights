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
import android.speech.tts.TextToSpeech;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;
import com.clarifai.api.exception.ClarifaiException;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

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

  private TextToSpeech txtspk;
  private String txt;

  private Uri imguri;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_recognition);

    textView = (TextView) findViewById(R.id.text_view);
    selectButton = (Button) findViewById(R.id.select_button);
    int CamId = findBackFacingCamera();
    camera = getCameraInstance(CamId);
    camView = new CSView(this, camera);
    prev = (FrameLayout) findViewById(R.id.cam_view);
    prev.addView(camView);

    selectButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        camView.capture(mCamera);
      }
    });
  }

  private int findBackFacingCamera() {
  int cameraId = 0;
  boolean cameraBack;
  // Search for the front facing camera
  int numberOfCameras = Camera.getNumberOfCameras();
  for (int i = 0; i < numberOfCameras; i++) {
    Camera.CameraInfo info = new Camera.CameraInfo();
    Camera.getCameraInfo(i, info);
    if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
      cameraId = i;
      cameraBack = true;
      break;
    }
  }
    return cameraId;
  }

  public static Camera getCameraInstance(int CamId){

    Camera c = null;
    try {
      c = Camera.open(CamId); // attempt to get a Camera instance
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
      //check number of files in directory and delete oldest file if number of files is > 9
      File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
              Environment.DIRECTORY_PICTURES), "Sights");

      File[] files = mediaStorageDir.listFiles();

      Arrays.sort(files, new Comparator<File>() {
        public int compare(File f1, File f2) {
          return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
        }
      });

      if( files.length > 5){
        try{

          if(files[0].delete()){
            System.out.println(files[0].getName() + " is deleted!");
          }else{
            System.out.println("Delete operation is failed.");
          }

        }catch(Exception e){

          e.printStackTrace();

        }
      }

      //end of directory work --Jason
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
        StringBuilder c = new StringBuilder();
        int i = 0;
        for (Tag tag : result.getTags()) {
          b.append(b.length() > 0 ? ", " : "").append(tag.getName());
          if(i<5) {
            c.append(c.length() > 0 ? ", " : "").append(tag.getName());
            i++;
          }
        }
        textView.setText("Tags:\n" + b);
        txt = c.toString();
        txtspk = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
          @Override public void onInit(int status) {
            txtspk.setLanguage(Locale.UK);
            txtspk.speak(txt, TextToSpeech.QUEUE_FLUSH, null);
          }
        });

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
