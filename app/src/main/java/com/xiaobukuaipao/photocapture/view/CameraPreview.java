package com.xiaobukuaipao.photocapture.view;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by xiaobu1 on 15-3-14.
 */
public class CameraPreview extends SurfaceView implements CameraPreviewAction, SurfaceHolder.Callback {
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

    // 闪光灯,默认是开启的
    private FlashMode mFlashMode = FlashMode.ON;

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
        // SurfaceView不维护自己的缓冲区，等待屏幕渲染引擎将内容推送到用户面前
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

    /**
     * 依赖类来设置Camera
     * @param camera
     * @param outer
     */
    public void setCamera(Camera camera, boolean outer) {
        if (outer) {
            mCamera = camera;
        }
    }

    /**
     * Begin the preview of the camera input
     */
    public void startCameraPreview() {
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置显示方向
     * @param camera
     * @param angle
     */
    private void setDisplayOrientation(Camera camera, int angle) {
        Method displayOrientation;
        try {
            displayOrientation = camera.getClass().getMethod("setDisplayOrientation", new Class[] {int.class});
            if (displayOrientation != null) {
                displayOrientation.invoke(camera, new Object[] {angle});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        try {
            Camera.Parameters parameters = mCamera.getParameters();
            // Set the auto-focus mode to "continuous"
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

            // Preview size must exist
            if (mPreviewSize != null) {
                Log.d(TAG, "set preview size");
                Camera.Size previewSize = mPreviewSize;
                parameters.setPreviewSize(previewSize.width, previewSize.height);
            } else {
                Log.d(TAG, "mPreviewSize is null");
            }

            // 设置camera预览的角度，因为默认图片是倾斜90度的, 所以需要解决图片旋转90度的问题
            // 这个方法在android2.2以上的版本才可以使用，在2.1及之前的是没有的
            // mCamera.setDisplayOrientation(90);
            // 利用反射机制来解决,待验证~
            if (Build.VERSION.SDK_INT >= 8) {
                setDisplayOrientation(mCamera, 90);
            } else {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    parameters.set("orientation", "portrait");
                    parameters.set("rotation", 90);
                }
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    parameters.set("orientation", "landscape");
                    parameters.set("rotation", 90);
                }
            }

            // 此处会发生错误
            // It is failing because not all devices support arbitrary(任意的) preview sizes
            // Apparently some do but you can't rely on it
            // http://stackoverflow.com/questions/3890381/camera-setparameters-failed-in-android
            mCamera.setParameters(parameters);

            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void	surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Dispose of the camera preview.
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    /**
     * Calculate the measurements of the layout
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            Log.d(TAG, "onMeasure mSupportedPreviewSizes is not null");
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        } else {
            Log.d(TAG, "onMeasure mSupportedPreviewSizes is null");
        }

        if (mPreviewSize != null) {
            Log.d(TAG, "onMeasure mPreviewSize is not null");
        } else {
            Log.d(TAG, "onMeasure mPreviewSize is null");
        }
    }

    /**
     * Update the layout based on rotation and orientation changes.
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        if (changed) {
            final int width = right - left;
            final int height = bottom - top;

            int previewWidth = width;
            int previewHeight = height;

            if (mPreviewSize != null){
                Display display = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

                switch (display.getRotation())
                {
                    case Surface.ROTATION_0:
                        previewWidth = mPreviewSize.height;
                        previewHeight = mPreviewSize.width;
                        mCamera.setDisplayOrientation(90);
                        break;
                    case Surface.ROTATION_90:
                        previewWidth = mPreviewSize.width;
                        previewHeight = mPreviewSize.height;
                        break;
                    case Surface.ROTATION_180:
                        previewWidth = mPreviewSize.height;
                        previewHeight = mPreviewSize.width;
                        break;
                    case Surface.ROTATION_270:
                        previewWidth = mPreviewSize.width;
                        previewHeight = mPreviewSize.height;
                        mCamera.setDisplayOrientation(180);
                        break;
                }
            }

            final int scaledChildHeight = previewHeight * width / previewWidth;
            mCameraView.layout(0, height - scaledChildHeight, width, height);
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {
        Camera.Size optimalSize = null;
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) height / width;

        // Try to find a size match which suits the whole screen minus the menu on the left.
        for (Camera.Size size : sizes) {
            if (size.height != width) continue;
            double ratio = (double) size.width / size.height;
            if (ratio <= targetRatio + ASPECT_TOLERANCE && ratio >= targetRatio - ASPECT_TOLERANCE){
                optimalSize = size;
            }
        }

        // If we cannot find the one that matches the aspect ratio, ignore the requirement.
        if (optimalSize == null) {
            // TODO : Backup in case we don't get a size.
        }

        return optimalSize;
    }

    private void setCameraDisplayOrientation(int rotation, int cameraId) {
        if (Build.VERSION.SDK_INT > 8) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);

            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0: degrees = 0; break;
                case Surface.ROTATION_90: degrees = 90; break;
                case Surface.ROTATION_180: degrees = 180; break;
                case Surface.ROTATION_270: degrees = 270; break;
            }

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }

            setDisplayOrientation(mCamera, result);
        }
    }

    @Override
    public void switchCamera(int rotation, int cameraId) {
        setCameraDisplayOrientation(rotation, cameraId);
        startCameraPreview();
    }


    // 闪光灯enum
    enum FlashMode {
        /** 闪光灯开启 */
        ON,
        /** 闪光灯关闭 */
        OFF,
        /** 闪光灯自动 */
        AUTO,
        /** 手电筒打开 */
        Torch
    }

}
