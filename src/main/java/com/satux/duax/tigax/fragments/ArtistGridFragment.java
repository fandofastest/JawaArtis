package com.satux.duax.tigax.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.satux.duax.tigax.GlobalDetailActivity;
import com.satux.duax.tigax.R;
import com.satux.duax.tigax.adapters.ArtistGridAdapter;
import com.satux.duax.tigax.utils.SongsUtils;

public class ArtistGridFragment extends Fragment {

    ListView listView;
    SongsUtils songsUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_artist_grid, container, false);

        listView = v.findViewById(R.id.listView);
        songsUtils = new SongsUtils(getActivity());

        listView.setAdapter((new ArtistGridAdapter(getActivity(), songsUtils.artists())));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                if (position >= 0) {
                    Intent intent = new Intent(getActivity(), GlobalDetailActivity.class);
                    intent.putExtra("id", position);
                    intent.putExtra("name", songsUtils.artists().get(position).get("artist"));
                    intent.putExtra("field", "artists");
                    startActivity(intent);
                }
            }
        });

        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }
}
