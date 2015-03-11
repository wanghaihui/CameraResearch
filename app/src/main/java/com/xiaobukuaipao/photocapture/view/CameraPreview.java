package com.xiaobukuaipao.photocapture.view;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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
    // private List


    public CameraPreview(Context context) {
        super(context);

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
