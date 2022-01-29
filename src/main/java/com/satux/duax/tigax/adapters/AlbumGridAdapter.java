package com.satux.duax.tigax.adapters;

import android.content.Context;
import android.graphics.Outline;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;

import com.satux.duax.tigax.R;
import com.satux.duax.tigax.models.SongModel;
import com.satux.duax.tigax.utils.ImageUtils;
import com.satux.duax.tigax.utils.SongsUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class AlbumGridAdapter extends BaseAdapter {
    private final ArrayList<HashMap<String, String>> mobileValues;
    @Nullable
    LayoutInflater inflater;
    SongsUtils songsUtils;
    private Context context;

    public AlbumGridAdapter(Context context,
                            ArrayList<HashMap<String, String>> mobileValues) {
        this.context = context;
        this.mobileValues = mobileValues;
        songsUtils = new SongsUtils(context);
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Nullable
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        ViewHolder holder;
        View gridView = convertView;

        if (convertView == null) {

            holder = new ViewHolder();
            gridView = inflater.inflate(R.layout.grid_item, parent, false);
            holder.text = gridView
                    .findViewById(R.id.grid_item_label);
            holder.subtext = gridView
                    .findViewById(R.id.grid_item_sublabel);
            holder.image = gridView
                    .findViewById(R.id.grid_item_image2);
            holder.imageOverflow = gridView.findViewById(R.id.imageOverflow);
            gridView.setTag(holder);
        } else {
            holder = (ViewHolder) gridView.getTag();
        }
        if (getCount() > 1) {
            // set value into textView
            holder.text.setText(mobileValues.get(position).get("album"));
            holder.subtext.setText(mobileValues.get(position).get("artist")
            );

            // set image based on selected text
            //imageView.setImageResource(R.drawable.windows_logo);

            final ArrayList<SongModel> albumSongs = songsUtils.albumSongs(mobileValues.get(position).get("album")
            );
            holder.image.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), Math.round(view.getHeight()), 40F);
                }
            });
            holder.image.setClipToOutline(true);
            (new ImageUtils(context)).setAlbumArt(albumSongs, holder.image);

            final PopupMenu pop = new PopupMenu(context, holder.imageOverflow);
            final int[] j = new int[5];
            j[0] = R.id.play_musicUtils;
            j[1] = R.id.play_next_musicUtils;
            j[2] = R.id.shuffle_play_musicUtils;
            j[3] = R.id.add_to_queue_musicUtils;
            j[4] = R.id.add_to_playlist_musicUtils;
            songsUtils.generateMenu(pop, j);
            pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.play_musicUtils:
                            songsUtils.play(0, albumSongs);
                            return true;
                        case R.id.play_next_musicUtils:
                            for (int i = albumSongs.size(); i > 0; i--) {
                                songsUtils.playNext(albumSongs.get(i - 1));
                            }
                            return true;
                        case R.id.add_to_queue_musicUtils:
                            for (int i = albumSongs.size(); i > 0; i--) {
                                songsUtils.addToQueue(albumSongs.get(i - 1));
                            }
                            return true;
                        case R.id.add_to_playlist_musicUtils:
                            songsUtils.addToPlaylist(albumSongs);
                            return true;
                        case R.id.shuffle_play_musicUtils:
                            songsUtils.shufflePlay(albumSongs);
                        default:
                            return false;
                    }
                }
            });

            holder.imageOverflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pop.show();
                }
            });
        }

        return gridView;
    }
    public int getCount() {
        if (mobileValues != null) {
            return mobileValues.size();
        }
        return 0;
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

    private static class ViewHolder {

        TextView text, subtext;
        ImageView image, imageOverflow;
    }
}
