package com.eleks.rnd.warehouse.glass.qr;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    
    private static final String TAG = "CameraPreview";

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.PreviewCallback mPreviewCallback;
    private Camera.AutoFocusCallback mAutoFocusCallback;

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(Camera camera, Camera.PreviewCallback previewCb, Camera.AutoFocusCallback autoFocusCb) {
        mCamera = camera;
        mPreviewCallback = previewCb;
        mAutoFocusCallback = autoFocusCb;

        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null) {
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            Log.d(TAG, "" + e);
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(mPreviewCallback);

            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size size = getBestPreviewSize(width, height, parameters);
            parameters.setPreviewSize(size.width, size.height);
            parameters.setPreviewFpsRange(30000, 30000);
            mCamera.setParameters(parameters);

            mCamera.startPreview();
            mCamera.autoFocus(mAutoFocusCallback);
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Log.d(TAG, "w=" + width + "h=" + height);
        // bigger picture for recognition purposes
        int nW = width * 2;
        int nH = height * 2;

        Camera.Size result = null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            Log.d(TAG, "Supported: " + "w=" + size.width + "\th=" + size.height);
            if (size.width <= nW && size.height <= nH) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        Log.d(TAG, "Selected: " + result.width + " x " + result.height);
        Log.d(TAG, "Focus modes: " + parameters.getSupportedFocusModes());
        return result;
    }
}