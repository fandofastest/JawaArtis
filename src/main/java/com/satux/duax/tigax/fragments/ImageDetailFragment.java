package com.satux.duax.tigax.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.satux.duax.tigax.R;
import com.satux.duax.tigax.utils.ImageUtils;
import com.satux.duax.tigax.utils.SongsUtils;

public class ImageDetailFragment extends Fragment {
    private static final String IMAGE_DATA_EXTRA = "resId";
    private int mImageNum;
    private ImageView mImageView;

    // Empty constructor, required as per Fragment docs
    public ImageDetailFragment() {
    }
    @NonNull
    public static ImageDetailFragment newInstance(int imageNum) {
        final ImageDetailFragment f = new ImageDetailFragment();
        final Bundle args = new Bundle();
        args.putInt(IMAGE_DATA_EXTRA, imageNum);
        f.setArguments(args);
        return f;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageNum = getArguments() != null ? getArguments().getInt(IMAGE_DATA_EXTRA) : -1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.play_activity_viewpager_image, container, false);
        mImageView = v.findViewById(R.id.imageView);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Runnable run = new Runnable() {
            @Override
            public void run() {
                (new ImageUtils(getContext())).getFullImageByPicasso(((new SongsUtils(getActivity())).queue()).get(mImageNum).getAlbumID(), mImageView);
            }
        };
        run.run();
    }
}
