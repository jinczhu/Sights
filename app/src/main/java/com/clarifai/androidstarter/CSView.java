package com.clarifai.androidstarter;

import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera;
import android.view.Window;
import android.view.WindowManager;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.*;

public class CSView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = RecognitionActivity.class.getSimpleName();
    private SurfaceHolder cHolder;
    public Camera camera;
    public boolean isPreviewRunning;

    public CSView(Context context, Camera mcamera){
        super(context);
        camera = mcamera;
        cHolder = getHolder();
        cHolder.addCallback(this);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        if (isPreviewRunning)
        {
            camera.stopPreview();
        }

        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        if(previewSizes.get(0).height<previewSizes.get(0).width){
            width=previewSizes.get(0).height;
            height=previewSizes.get(0).width;
        }
        else{
            height=previewSizes.get(0).height;
            width=previewSizes.get(0).width;
        }

        Context mContext = getContext();

        WindowManager win;
        String wanted = Context.WINDOW_SERVICE;
        win = (WindowManager)mContext.getSystemService(wanted);

        Display display = win.getDefaultDisplay();
        if(display.getRotation() == Surface.ROTATION_0)
        {
            parameters.setPreviewSize(height, width);
            camera.setDisplayOrientation(90);
        }

        if(display.getRotation() == Surface.ROTATION_90)
        {
            parameters.setPreviewSize(width, height);
        }

        if(display.getRotation() == Surface.ROTATION_180)
        {
            parameters.setPreviewSize(height, width);
        }

        if(display.getRotation() == Surface.ROTATION_270)
        {
            parameters.setPreviewSize(width, height);
            camera.setDisplayOrientation(180);
        }
        if (parameters.getSupportedFocusModes().contains(                               //always autofocus if device has this mode
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        camera.setParameters(parameters);
        previewCamera();
    }

    public void previewCamera()
    {
        try
        {
            camera.setPreviewDisplay(cHolder);
            camera.startPreview();
            isPreviewRunning = true;
        }
        catch(Exception e)
        {
            Log.d(TAG, "Cannot start preview", e);
        }
    }

    public void surfaceCreated(SurfaceHolder holder){
        try{
            camera.setPreviewDisplay(cHolder);
            camera.startPreview();
        }catch(Exception e){

        }
    }

    public void surfaceDestroyed(SurfaceHolder holder){
        camera.stopPreview();
        camera = null;
    }

    public void capture(final Camera.PictureCallback bithandler) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                camera.startPreview();
                camera.takePicture(null, null, bithandler);
            }
        }, 0, 5000);
    }
}
