package com.xiaobukuaipao.photocapture.fragment;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.xiaobukuaipao.photocapture.R;

import java.io.IOException;
import java.util.List;

/**
 * Activities that contain this fragment must implement the
 * {@link PhotoCaptureFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PhotoCaptureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhotoCaptureFragment extends PhotoCaptureBaseFragment {
    private static final String TAG = PhotoCaptureFragment.class.getSimpleName();

    private int itemNumber;

    private OnFragmentInteractionListener mListener;

    // 内置Camera
    private Camera mCamera;
    // View to display the camera output
    private CameraPreview mPreview;
    // Reference to the containing view
    private View mCameraView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment PhotoCaptureFragment.
     */
    public static PhotoCaptureFragment newInstance(int item_number) {
        PhotoCaptureFragment fragment = new PhotoCaptureFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ITEM_NUMBER, item_number);
        fragment.setArguments(args);
        return fragment;
    }

    public PhotoCaptureFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            itemNumber = getArguments().getInt(ARG_ITEM_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo_capture, container, false);
        // Create our preview and set it as the content of our activity
        boolean opened = safeCameraOpenInView(view);
        if (opened == false) {
            Log.d(TAG, "Camera failed to open");
            return view;
        }

        setUIListeners(view);

        return view;
    }

    /**
     * 设置监听器
     * @param view
     */
    private void setUIListeners(View view) {
        Button captureButton = (Button) view.findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get an image from the camera
                // mCamera.takePicture(null, null, mPicture);
            }
        });
    }

    /**
     * Recommended "safe" way to open the camera
     * @param view
     * @return
     */
    private boolean safeCameraOpenInView(View view) {
        boolean opened = false;
        releaseCameraAndPreview();
        mCamera = getCameraInstance();
        mCameraView = view;
        opened = (mCamera != null);

        if (opened == true) {
            mPreview = new CameraPreview(getActivity().getBaseContext(), mCamera, view);
            FrameLayout preview = (FrameLayout) view.findViewById(R.id.camera_preview);
            preview.addView(mPreview);
            mPreview.startCameraPreview();
        }

        return opened;
    }

    /**
     * Clear any existing camera and preview
     */
    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mPreview != null) {
            // 销毁绘制缓存
            mPreview.destroyDrawingCache();
            mPreview.mCamera = null;
        }
    }

    /**
     * Safe method for getting a camera instance
     * @return
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            // attempt to get a camera instance
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // returns null if camera is unavailable
        return c;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 释放Camera
        releaseCameraAndPreview();
    }


    class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
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
                    Camera.Size previewSize = mPreviewSize;
                    parameters.setPreviewSize(previewSize.width, previewSize.height);
                }

                // 此处会发生错误
                // It is failing because not all devices support arbitrary(任意的) preview sizes
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
                // 设置camera预览的角度，因为默认图片是倾斜90度的, 所以需要解决图片旋转90度的问题
                // 这个方法在android2.2以上的版本才可以使用，在2.1及之前的是没有的
                mCamera.setDisplayOrientation(90);

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
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
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

    }

}
