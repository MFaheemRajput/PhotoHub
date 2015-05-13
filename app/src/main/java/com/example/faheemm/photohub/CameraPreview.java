package com.example.faheemm.photohub;

import android.content.Context;
import android.hardware.Camera;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.IOException;

/**
 * Created by faheem.m on 03/04/2015.
 */

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Fragment mFragmentParam;

    public CameraPreview(Context context, Camera camera,Fragment fragmentParam) {
        super(context);
        mCamera = camera;
        mCamera.setDisplayOrientation(90);
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


    }

    public void setNullCamera(){

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {


            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(CameraPreview.this.getWidth(), CameraPreview.this.getHeight());
            //mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            mCamera.setDisplayOrientation(90);

        } catch (IOException e) {
            Log.d(String.valueOf(1), "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        CameraPreview.this.setLayoutParams(new FrameLayout.LayoutParams(width, height));

       if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(CameraPreview.this.getWidth(), CameraPreview.this.getHeight());
            //mCamera.setParameters(parameters);
            mCamera.startPreview();


        } catch (Exception e){
            Log.d(String.valueOf(1), "Error starting camera preview: " + e.getMessage());
        }
    }

}