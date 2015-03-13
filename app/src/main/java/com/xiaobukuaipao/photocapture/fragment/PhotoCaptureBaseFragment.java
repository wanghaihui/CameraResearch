package com.xiaobukuaipao.photocapture.fragment;

import android.net.Uri;
import android.support.v4.app.Fragment;

/**
 * Created by xiaobu1 on 15-3-13.
 */
public class PhotoCaptureBaseFragment extends Fragment {
    public static final String ARG_ITEM_NUMBER = "ARG_ITEM_NUMBER";

    /**
     * Default empty constructor
     */
    public PhotoCaptureBaseFragment() {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

}
