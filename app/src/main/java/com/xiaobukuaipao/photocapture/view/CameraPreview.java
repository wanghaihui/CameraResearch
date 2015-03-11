package com.xiaobukuaipao.photocapture.view;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.List;


/**
 * Created by xiaobu1 on 15-3-11.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
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
        
        requestLayout();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void	surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

}
