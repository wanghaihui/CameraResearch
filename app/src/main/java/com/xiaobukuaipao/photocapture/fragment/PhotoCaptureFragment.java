package com.xiaobukuaipao.photocapture.fragment;

import android.app.Activity;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.xiaobukuaipao.photocapture.R;
import com.xiaobukuaipao.photocapture.view.CameraPreview;
import com.xiaobukuaipao.photocapture.view.CameraPreviewAction;

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

    // 前置后置摄像头
    private boolean mIsFrontCamera = false;
    private int currentCameraId;

    private CameraPreviewAction mPreviewActionListener;
    public void setPreviewActionListener(CameraPreviewAction mPreviewActionListener) {
        this.mPreviewActionListener = mPreviewActionListener;
    }

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
        /**
         * 拍照
         */
        ImageButton captureButton = (ImageButton) view.findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get an image from the camera
                // mCamera.takePicture(null, null, mPicture);
            }
        });

        /**
         * 切换摄像头
         */
        ImageButton switchCameraBtn = (ImageButton) view.findViewById(R.id.switch_camera);
        switchCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });
    }

    /**
     * 切换摄像头
     */
    private void switchCamera() {
        boolean opened = false;
        mIsFrontCamera = !mIsFrontCamera;
        releaseCameraAndPreview();
        mCamera = getCameraInstance();
        opened = (mCamera != null);
        if (opened == true) {
            if (mCameraView != null) {
                mPreview.setCamera(mCamera, true);

                mPreviewActionListener.switchCamera(this.getActivity().getWindowManager().getDefaultDisplay()
                        .getRotation(), currentCameraId);
            }
        }
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
            mPreview = new CameraPreview(getActivity().getBaseContext(), mCamera, mCameraView);
            FrameLayout preview = (FrameLayout) mCameraView.findViewById(R.id.camera_preview);
            preview.addView(mPreview);

            setPreviewActionListener(mPreview);

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
            mPreview.setCamera(null, true);
        }
    }

    /**
     * Safe method for getting a camera instance
     * @return
     */
    public Camera getCameraInstance() {
        Camera c = null;

        if (!mIsFrontCamera) {
            // 后置摄像头
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            try {
                // attempt to get a camera instance
                c = Camera.open();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // API 8(2.2)
            // 前置摄像头从API 9(2.3)开始支持
            if (Build.VERSION.SDK_INT > 8) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                        try {
                            c = Camera.open(i);
                        } catch (Exception e) {
                            c = null;
                        }
                    }
                }
            }
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

}
