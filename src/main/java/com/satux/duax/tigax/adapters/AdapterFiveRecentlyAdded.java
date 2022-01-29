package com.satux.duax.tigax.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Outline;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;

import com.satux.duax.tigax.R;
import com.satux.duax.tigax.models.SongModel;
import com.satux.duax.tigax.utils.ImageUtils;
import com.satux.duax.tigax.utils.SongsUtils;

import java.util.ArrayList;

public class AdapterFiveRecentlyAdded extends BaseAdapter implements OnClickListener {

    private Activity activity;
    private ArrayList<SongModel> data;
    private SongsUtils songsUtils;

    public AdapterFiveRecentlyAdded(Activity a) {

        songsUtils = new SongsUtils(a);
        activity = a;
        data = new ArrayList<>(getData());
    }

    @NonNull
    private ArrayList<SongModel> getData() {
        // ArrayList<SongModel> newSongs = songsUtils.newSongs();
        ArrayList<SongModel> newSongs = songsUtils.allServer();
        ArrayList<SongModel> list = new ArrayList<>();
        int items = 25;
        if (newSongs.size() > items) {
            for (int i = 0; i < items; i++) {
                list.add(newSongs.get(i));
            }
        } else {
            list.addAll(newSongs);
        }
        return list;
    }

    /**
     * ***** What is the size of Passed Arraylist Size ***********
     */
    public int getCount() {

        if (data.size() <= 0)
            return 1;
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    /**
     * *** Depends upon data size called for each row , Create each ListView row ****
     */
    @Nullable
    @SuppressLint("SimpleDateFormat")
    public View getView(final int position, @Nullable View vi, ViewGroup parent) {
        final ViewHolder holder;

        if (vi == null) {
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.row, parent, false);

            holder = new ViewHolder();
            holder.text = vi.findViewById(R.id.text);
            holder.text1 = vi.findViewById(R.id.text1);
            holder.image = vi.findViewById(R.id.image);
            holder.imageOverflow = vi.findViewById(R.id.albumArtImageView);
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        if (data.size() > 0) {

            /***** Get each Model object from Arraylist ********/
            final SongModel tempValues = data.get(position);
            final String duration = tempValues.getDuration();
            final String artist = tempValues.getArtist();
            final String songName = tempValues.getFileName();
            final String title = tempValues.getTitle();

            String finalTitle;
            if (title != null) {
                finalTitle = title;
            } else {
                finalTitle = songName;
            }

            holder.image.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), Math.round(view.getHeight()), 20F);
                }
            });
            holder.image.setClipToOutline(true);

            (new ImageUtils(activity)).setAlbumArt(tempValues.getAlbumID(), holder.image);

            holder.text.setText(finalTitle);
            holder.text1.setText(((artist.length() > 25) ? artist.substring(0, 25) : artist) + "; " + duration);

            vi.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    songsUtils.play(position, songsUtils.allServer());
                }
            });
            final PopupMenu pop = new PopupMenu(activity, holder.imageOverflow);
            int[] j = new int[5];
            j[0] = R.id.play_next_musicUtils;
            j[1] = R.id.shuffle_play_musicUtils;
            j[2] = R.id.add_to_queue_musicUtils;
            j[3] = R.id.add_to_playlist_musicUtils;
            j[4] = R.id.info_musicUtils;
            songsUtils.generateMenu(pop, j);
            pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.info_musicUtils:
                            songsUtils.info(data.get(position)).show();
                            return true;
                        case R.id.play_next_musicUtils:
                            songsUtils.playNext(data.get(position));
                            return true;
                        case R.id.add_to_queue_musicUtils:
                            songsUtils.addToQueue(data.get(position));
                            return true;
                        case R.id.add_to_playlist_musicUtils:
                            songsUtils.addToPlaylist(data.get(position));
                            return true;
                        case R.id.shuffle_play_musicUtils:
                            songsUtils.shufflePlay(position, songsUtils.newSongs());
                            return true;
                        default:
                            return false;
                    }
                }
            });

            holder.imageOverflow.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    pop.show();
                }
            });
            vi.setVisibility(View.VISIBLE);
        } else {
            vi.setVisibility(View.INVISIBLE);
        }
        return vi;
    }
    @Override
    public void onClick(View arg0) {
    }

    /**
     * ****** Create a holder Class to contain inflated xml file elements ********
     */
    private static class ViewHolder {

        TextView text;
        TextView text1;
        ImageView image, imageOverflow;
    }
}
