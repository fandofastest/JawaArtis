package com.satux.duax.tigax.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.satux.duax.tigax.R;
import com.satux.duax.tigax.utils.SongsUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class PlaylistFragmentAdapterSimple extends BaseAdapter {

    SongsUtils songsUtils;
    private ArrayList<HashMap<String, String>> data;
    @Nullable
    private LayoutInflater inflater;

    public PlaylistFragmentAdapterSimple(Context context) {

        songsUtils = new SongsUtils(context);
        data = songsUtils.getAllPlaylists();

        /*********** Layout inflator to call external xml layout () ***********/
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        data.clear();
        data = songsUtils.getAllPlaylists();
    }

    /**
     * ***** What is the size of Passed Arraylist Size ***********
     */
    public int getCount() {

        if (data.size() <= 0)
            return 1;
        return data.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
    /**
     * *** Depends upon data size called for each row , Create each ListView row ****
     */
    @Nullable
    public View getView(final int position, @Nullable View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;

        if (convertView == null) {

            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
            vi = inflater.inflate(R.layout.playlist_row_simple, parent, false);

            /****** View Holder Object to contain tabitem.xml file elements ******/

            holder = new ViewHolder();
            holder.text = vi.findViewById(R.id.titleTextView);

            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        if (data.size() <= 0) {
            vi.setVisibility(View.GONE);
        } else {
            vi.setVisibility(View.VISIBLE);

            holder.text.setText(data.get(position).get("title"));
        }

        return vi;
    }

    private static class ViewHolder {

        public TextView text;
    }
}
