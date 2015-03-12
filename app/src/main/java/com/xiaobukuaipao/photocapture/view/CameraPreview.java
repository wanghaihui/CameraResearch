package com.xiaobukuaipao.photocapture.view;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;
import java.util.List;


/**
 * Created by xiaobu1 on 15-3-11.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = CameraPreview.class.getSimpleName();
    // SurfaceHolder
    private SurfaceHolder mHolder;
    // Our Camera
    private Camera mCamera;
    // Parent Context
    private Context mContext;
    // Camera Sizing (For rotation, orientation changes)
    private Camera.Size mPreviewSize;
    // List of supported preview sizes
    private List<Camera.Size> mSupportedPreviewSizes;
    // Flash modes supported by this camera
    private List<String> mSupportedFlashModes;
    // View holding this camera
    private View mCameraView;

    public CameraPreview(Context context, Camera camera, View cameraView) {
        super(context);
        // Capture the context
        mCameraView = cameraView;
        mContext = context;
        setCamera(camera);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // 保持屏幕始终开着
        mHolder.setKeepScreenOn(true);

        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * Extract supported preview and flash modes from the camera
     * @param camera
     */
    private void setCamera(Camera camera) {
        mCamera = camera;
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        mSupportedFlashModes = mCamera.getParameters().getSupportedFlashModes();
        // Set the camera to auto flash mode
        if (mSupportedFlashModes != null && mSupportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            mCamera.setParameters(parameters);
        }

        // 强制重绘,从measure开始
        requestLayout();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        //
    }

    @Override
    public void	surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview : " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Dispose of the camera preview.
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

}
