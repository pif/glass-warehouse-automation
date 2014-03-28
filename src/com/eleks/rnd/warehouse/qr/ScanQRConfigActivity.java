package com.eleks.rnd.warehouse.qr;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.eleks.rnd.warehouse.glass.Flow;
import com.eleks.rnd.warehouse.glass.R;

/**
 * Based on
 * https://github.com/destil/glasquare/blob/master/code/Glasquare/src/main
 * /java/cz/destil/glasquare/activity/QrScanActivity.java
 * 
 */
public class ScanQRConfigActivity extends Activity {

    public static final String EXTRA_TEXT = "text";

    private static final String TAG = "ScanQRActivity";

    private ImageScanner scanner;
    private Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            // Log.d(TAG, "Preview Format: " + parameters.getPreviewFormat());
            // Log.d(TAG, "onPreviewFrame: size: " + size.width +"x"+
            // size.height);

            Image barcode = new Image(size.width, size.height, "NV21");
            barcode.setData(data);
            barcode = barcode.convert("Y800");
            // barcode.setCrop(
            // size.width/2-size.width/6,
            // size.height/2-size.height/6,
            // size.width/2+size.width/6,
            // size.height/2+size.height/6
            // );
            // Log.d(TAG, "picture size: " + barcode.getWidth()+" " +
            // barcode.getHeight() + " " + barcode.getFormat() + " " +
            // barcode.toString());
            // Log.d(TAG, "picture crop: " +
            // Arrays.toString(barcode.getCrop()));

            int result = scanner.scanImage(barcode);

            if (result != 0) {
                previewing = false;
                releaseCamera();

                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {
                    Flow.with(ScanQRConfigActivity.this).scanned(sym);
                    break;
                }
            }
        }
    };
    // Mimic continuous auto-focusing
    private Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 2000);
        }
    };
    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    private boolean previewing = true;

    static {
        System.loadLibrary("iconv");
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
        }
    };

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        // for my xperia
        // Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        // for (int cameraIndex = 0; cameraIndex < Camera.getNumberOfCameras();
        // cameraIndex++) {
        // Camera.getCameraInfo(cameraIndex, cameraInfo);
        // Log.d(TAG, "opening camera: " + cameraIndex + " " + cameraInfo);
        // if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
        // try {
        // return Camera.open(cameraIndex);
        // } catch (RuntimeException e) {
        // e.printStackTrace();
        // }
        // }
        // }
        // for glass
        Camera c = null;
        try {
            c = Camera.open(0);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "" + e);
        }
        Log.d(TAG, "returning camera: " + c);
        return c;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == Flow.VOICE_DATE_REQUEST) {
            Flow.with(this).completeWithDate(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);

        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        autoFocusHandler = new Handler();

        // initCamera();
    }

    private void initCamera() {
        previewing = true;
        mCamera = getCameraInstance();
        if (mCamera == null) {
            Toast.makeText(this, "Camera is not available at the moment. Restart Glass.", Toast.LENGTH_LONG).show();
            finish();
        }

        mPreview = (CameraPreview) findViewById(R.id.camera_preview);
        mPreview.init(mCamera, previewCb, autoFocusCB);
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

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

}