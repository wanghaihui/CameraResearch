package com.xiaobukuaipao.photocapture;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.xiaobukuaipao.photocapture.fragment.PhotoCaptureBaseFragment;
import com.xiaobukuaipao.photocapture.fragment.PhotoCaptureFragment;


public class PhotoCapture2Activity extends FragmentActivity implements PhotoCaptureBaseFragment.OnFragmentInteractionListener{
    private static final String TAG = PhotoCapture2Activity.class.getSimpleName();
    /**
     * Fragment Identifiers
     */
    public static final int PHOTO_CAPTURE_FRAGMENT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_photo_capture2);

        if (null == savedInstanceState) {
            // Android 5.0以前的版本
            if (Build.VERSION.SDK_INT < 21) {
                Log.i(TAG, "PhotoCaptureFragment new instance");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, PhotoCaptureFragment.newInstance(PHOTO_CAPTURE_FRAGMENT + 1))
                        .commit();
            } else if (Build.VERSION.SDK_INT >= 21){
                // Android 5.0以后的版本
                /*getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, PhotoCapture2Fragment.newInstance())
                        .commit();*/
            }
        }
    }


    public void onFragmentInteraction(Uri uri) {

    }

}
