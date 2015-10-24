package com.clarifai.androidstarter;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera;

public class CSView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder cHolder;
    public Camera camera;

    public CSView(Context context, Camera mcamera){
        super(context);
        camera = mcamera;
        cHolder = getHolder();
        cHolder.addCallback(this);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
        if(cHolder.getSurface() == null){
            return;
        }

        try{
            camera.stopPreview();
        }catch (Exception e){

        }

        try{
            camera.setPreviewDisplay(cHolder);
            camera.startPreview();
        }catch(Exception e) {

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
        //camera.stopPreview();
        //camera = null;
    }

    public void capture(Camera.PictureCallback bithandler){
        camera.takePicture(null, null, bithandler);
    }
}
