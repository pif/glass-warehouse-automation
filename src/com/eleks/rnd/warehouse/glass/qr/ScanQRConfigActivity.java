package com.eleks.rnd.warehouse.glass.qr;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.eleks.rnd.warehouse.glass.Flow;
import com.eleks.rnd.warehouse.glass.R;

/**
 * Based on https://github.com/destil/glasquare QR scanning activity.
 */
public class ScanQRConfigActivity extends Activity {

    private static final String TAG = "ScanQRConfigActivity";

    static {
        System.loadLibrary("iconv");
    }
    
    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PreviewCallback mPreviewCB = createPreviewCallback();
    private Camera.AutoFocusCallback mAutoFocusCB = createAutoFocusCallback();
    private Handler autoFocusHandler;
    private ImageScanner mScanner;

    private boolean mPreviewing = true;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);

        mScanner = new ImageScanner();
        mScanner.setConfig(0, Config.X_DENSITY, 3);
        mScanner.setConfig(0, Config.Y_DENSITY, 3);

        autoFocusHandler = new Handler();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == Flow.VOICE_DATE_REQUEST) {
            Flow.with(this).completeWithDate(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
    }

    public void onPause() {
        super.onPause();
        releaseCamera();
    }
    
    private void initCamera() {
        mPreviewing = true;
        mCamera = getCamera();
        if (mCamera == null) {
            Toast.makeText(this, "Camera is not available at the moment. Restart Glass.", Toast.LENGTH_LONG).show();
            finish();
        }

        mPreview = (CameraPreview) findViewById(R.id.camera_preview);
        mPreview.init(mCamera, mPreviewCB, mAutoFocusCB);
    }
    
    /**
     * A safe way to get an instance of the Camera object.
     */
    private static Camera getCamera() {
        Camera c = null;
        try {
            c = Camera.open(0);
        } catch (Exception e) {
            Log.d(TAG, "" + e);
        }
        Log.d(TAG, "returning camera: " + c);
        return c;
    }       
    
    private void releaseCamera() {
        if (mCamera != null) {
            mPreviewing = false;
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }    
    
    /**
     * Mimic continuous autofocus.
     * 
     * @return
     */
    private AutoFocusCallback createAutoFocusCallback() {
        final Runnable doAutoFocus = new Runnable() {
            public void run() {
                if (mPreviewing)
                    mCamera.autoFocus(mAutoFocusCB);
            }
        };
        AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
            public void onAutoFocus(boolean success, Camera camera) {
                autoFocusHandler.postDelayed(doAutoFocus, 2000);
            }
        };
        return autoFocusCallback;
    }

    private PreviewCallback createPreviewCallback() {
        PreviewCallback previewCallback = new Camera.PreviewCallback() {
            public void onPreviewFrame(byte[] data, Camera camera) {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = parameters.getPreviewSize();

                Image barcode = new Image(size.width, size.height, "NV21");
                barcode.setData(data);
                barcode = barcode.convert("Y800");
                int result = mScanner.scanImage(barcode);

                if (result != 0) {
                    mPreviewing = false;
                    releaseCamera();

                    SymbolSet syms = mScanner.getResults();
                    for (Symbol sym : syms) {
                        Flow.with(ScanQRConfigActivity.this).scanned(sym);
                        break;
                    }
                }
            }
        };

        return previewCallback;
    }
}