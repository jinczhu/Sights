package com.clarifai.androidstarter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;
import com.clarifai.api.exception.ClarifaiException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class TrainerActivity extends Activity {
    private static final String TAG = TrainerActivity.class.getSimpleName();
    private static final String APP_ID = "vM05qo55uhZard2dL4BixmMm4WsHIl6CsGCTgS_7";
    private static final String APP_SECRET = "rx4oPPiXiCWNRVcoJ0huLz02cKiQUZtq5JPVrhjM";

    private static final int CODE_PICK_IMG = 1;
    private static final int CODE_PICK_SPK = 2;

    private final ClarifaiClient client = new ClarifaiClient(APP_ID, APP_SECRET);
    private Button selectButton;
    private Button speechButton;
    private ImageView imageView;
    private TextView textView;
    private EditText textBox;
    private ArrayList<String> boxxy_text;
    private String boxxy_textS;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trainer_recognition);
        imageView = (ImageView) findViewById(R.id.image_view);
        textView = (TextView) findViewById(R.id.text_view);
        textBox = (EditText) findViewById(R.id.text_box);
        selectButton = (Button) findViewById(R.id.select_button);
        speechButton = (Button) findViewById(R.id.speech_button);

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CODE_PICK_IMG);
            }
        });
        speechButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(isConnected()) {
                    final Intent intentS = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intentS.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    startActivityForResult(intentS, CODE_PICK_SPK);
                } else{
                    Toast.makeText(getApplicationContext(), "Plese Connect to Internet", Toast.LENGTH_LONG).show();
                }
            }
        });
        textBox.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    //PERFORM THINGS.
                }
                return false;
            }
        });
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == CODE_PICK_IMG && resultCode == RESULT_OK) {
            // The user picked an image. Send it to Clarifai for recognition.
            Log.d(TAG, "User picked image: " + intent.getData());
            Bitmap bitmap = loadBitmapFromUri(intent.getData());
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                selectButton.setEnabled(false);

                // Run recognition on a background thread since it makes a network call.
                new AsyncTask<Bitmap, Void, RecognitionResult>() {
                    @Override
                    protected RecognitionResult doInBackground(Bitmap... bitmaps) {
                        return recognizeBitmap(bitmaps[0]);
                    }

                    @Override
                    protected void onPostExecute(RecognitionResult result) {
                        updateUIForResult(result);
                    }
                }.execute(bitmap);
            }
        } else if(requestCode == CODE_PICK_SPK && resultCode == RESULT_OK){

                boxxy_text = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                boxxy_textS = boxxy_text.get(0).toString();
                textBox.setText(boxxy_textS, TextView.BufferType.EDITABLE);
        } else {
            textView.setText("Unable to load selected image.");
        }
    }

    public  boolean isConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        if (net!=null && net.isAvailable() && net.isConnected()) {
            return true;
        } else {
            return false;
        }
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
            while (opts.outWidth / (2 * sampleSize) >= imageView.getWidth() &&
                    opts.outHeight / (2 * sampleSize) >= imageView.getHeight()) {
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