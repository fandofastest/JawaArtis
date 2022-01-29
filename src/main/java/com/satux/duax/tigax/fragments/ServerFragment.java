package com.satux.duax.tigax.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.commonsware.cwac.merge.MergeAdapter;
import com.satux.duax.tigax.R;
import com.satux.duax.tigax.adapters.CustomAdapter;
import com.satux.duax.tigax.models.SongModel;
import com.satux.duax.tigax.utils.SongsUtils;

import java.util.ArrayList;

public class ServerFragment extends Fragment {

    static int selectedRow;
    ListView list;
    @Nullable
    CustomAdapter adapter;
    @NonNull
    ArrayList<SongModel> CustomListViewValuesArr = new ArrayList<>();
    @Nullable
    SongsUtils songsUtils;
    @Nullable
    Context context;
    LayoutInflater mInflater;
    boolean _areLecturesLoaded = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        mInflater = LayoutInflater.from(context);
    }
    @SuppressLint({"SdCardPath", "SimpleDateFormat"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_songs, container, false);

        songsUtils = new SongsUtils(getActivity());

        setListData();

        list = v.findViewById(R.id.listView1);

        adapter = new CustomAdapter(getActivity(), CustomListViewValuesArr);

        MergeAdapter myMergeAdapter = new MergeAdapter();
        myMergeAdapter.addAdapter(adapter);

        list.setAdapter(myMergeAdapter);
        try {
            list.setSelectionFromTop(selectedRow, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return v;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !_areLecturesLoaded) {

            _areLecturesLoaded = true;
        }
    }

    public void setListData() {
        CustomListViewValuesArr.clear();
        CustomListViewValuesArr = new ArrayList<>(songsUtils.allServer());
    }
}
