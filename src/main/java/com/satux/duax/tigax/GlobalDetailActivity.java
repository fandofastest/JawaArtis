package com.satux.duax.tigax;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.satux.duax.tigax.adapters.RecyclerViewAdapter;
import com.satux.duax.tigax.callbacks.AsyncTaskCompletionCallback;
import com.satux.duax.tigax.models.SongModel;
import com.satux.duax.tigax.utils.CommonUtils;
import com.satux.duax.tigax.utils.ImageUtils;
import com.satux.duax.tigax.utils.SharedPrefsUtils;
import com.satux.duax.tigax.utils.SongsUtils;

import java.util.ArrayList;
import java.util.Objects;

public class GlobalDetailActivity extends AppCompatActivity implements AsyncTaskCompletionCallback {

    @Nullable
    RecyclerViewAdapter adapter;
    @NonNull
    ArrayList<SongModel> songsList = new ArrayList<>();
    @Nullable
    String field = "albums", raw = "A Sky Full Of Stars";
    @Nullable
    PerformBackgroundTasks performBackgroundTasks = null;
    SongsUtils songsUtils;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_detail);

        Toolbar toolbar = findViewById(R.id.anim_toolbar);
        setSupportActionBar(toolbar);

        int accentColor = (new CommonUtils(this)).accentColor(new SharedPrefsUtils(this));

        ((TextView) findViewById(R.id.category)).setTextColor(ContextCompat.getColor(this, accentColor));

        field = Objects.requireNonNull(getIntent().getExtras()).getString("field");
        raw = Objects.requireNonNull(getIntent().getExtras()).getString("name");

        performBackgroundTasks = new PerformBackgroundTasks(this, this, field);

        findViewById(R.id.spinner).setVisibility(View.INVISIBLE);

        ((TextView) findViewById(R.id.title)).setText(raw);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        songsUtils = new SongsUtils(this);
        setListData();

        int playbackTime = getPlayBackTime(songsList);
        String netPlayback = playbackTime + " mins";
        if (playbackTime > 60) {
            netPlayback = playbackTime / 60 + "h " + playbackTime % 60 + "m";
        }
        int numSongs = songsList.size();
        if (numSongs > 0) {
            ((TextView) findViewById(R.id.listInfoTextView))
                    .setText(numSongs + ((songsList.size() > 1) ? " tracks, " : " track, ") +
                            netPlayback + " playback");
        }

        findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songsUtils.play(0, songsList);
            }
        });

        findViewById(R.id.shuffle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songsUtils.shufflePlay(songsList);
            }
        });

        if (getResources().getBoolean(R.bool.isLandscape)) {
            getSupportActionBar().setTitle(" ");
        } else {
            CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
            collapsingToolbar.setTitle(" ");
            collapsingToolbar.setContentScrimColor(ContextCompat.getColor(this, R.color.primaryColor));
        }

        ImageView albumArtImageView = findViewById(R.id.header);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager;
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        (new ImageUtils(this)).setAlbumArt(songsList, albumArtImageView);
        adapter = new RecyclerViewAdapter(songsList, this, field);
        recyclerView.setAdapter(adapter);
    }

    /*
     * Gives output in minutes, getDuration() format is mm:ss
     */
    private int getPlayBackTime(ArrayList<SongModel> albumSongs) {
        int pTime = 0;
        for (int i = 0; i < albumSongs.size(); i++) {
            String duration = albumSongs.get(i).getDuration();
            pTime += Integer.parseInt(duration.split(":")[0]) * 60 +
                    Integer.parseInt(duration.split(":")[1]);
        }
        return pTime / 60;
    }

    /*
     * Grabbing data for RecyclerView
     */

    public void setListData() {
        songsList.clear();
        switch (field) {
            case "albums":
                songsList.addAll(songsUtils.albumSongs(raw));
                ((TextView) findViewById(R.id.category)).setText(getString(R.string.album_cap));
                break;
            case "mostplayed":
                songsList.addAll(songsUtils.mostPlayedSongs());
                ((TextView) findViewById(R.id.category)).setText(getString(R.string.auto_plalist_cap));
                if (performBackgroundTasks.getStatus() != AsyncTask.Status.RUNNING)
                    performBackgroundTasks.execute();
                break;
            case "favourites":
                songsList.addAll(songsUtils.favouriteSongs());
                ((TextView) findViewById(R.id.category)).setText(getString(R.string.auto_plalist_cap));
                if (performBackgroundTasks.getStatus() != AsyncTask.Status.RUNNING)
                    performBackgroundTasks.execute();
                break;
            case "artists":
                songsList.addAll(songsUtils.artistSongs(raw));
                ((TextView) findViewById(R.id.category)).setText(getString(R.string.artist_cap));
                break;
            case "recent":
                songsList.addAll(songsUtils.newSongs());
                ((TextView) findViewById(R.id.category)).setText(getString(R.string.recently_added_cap));
                break;
            default:
                songsList.addAll(songsUtils.playlistSongs(Integer.parseInt(field)));
                ((TextView) findViewById(R.id.category)).setText(getString(R.string.playlist_cap));
                if (performBackgroundTasks.getStatus() != AsyncTask.Status.RUNNING)
                    performBackgroundTasks.execute();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.global_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_searchBtn:
                Intent intent = new Intent(GlobalDetailActivity.this, SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.play_next:
                if (songsList.size() > 0) {
                    songsUtils.playNext(songsList);
                    (new CommonUtils(this)).showTheToast("List added for playing next");
                } else {
                    (new CommonUtils(this)).showTheToast("Error adding empty song list to queue");
                }
                break;
            case R.id.add_to_queue:
                songsUtils.addToQueue(songsList);
                break;
            case android.R.id.home:
                if (performBackgroundTasks.getStatus() == AsyncTask.Status.RUNNING) {
                    performBackgroundTasks.cancel(true);
                }
                backPressed();
                break;
            case R.id.add_to_playlist:
                songsUtils.addToPlaylist(songsList);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            backPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void backPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (performBackgroundTasks.getStatus() == AsyncTask.Status.RUNNING) {
            performBackgroundTasks.cancel(true);
        }
    }

    @Override
    public void updateViews() {
        findViewById(R.id.spinner).setVisibility(View.INVISIBLE);
        findViewById(R.id.recyclerView).setActivated(true);
        setListData();
        adapter.notifyDataSetChanged();
    }

    private static class PerformBackgroundTasks extends AsyncTask<String, Integer, Long> {

        private SongsUtils songsUtils;
        private String field;
        @NonNull
        private String TAG = "GlobalActivityAsyncTaskConsole";
        private AsyncTaskCompletionCallback callback;

        PerformBackgroundTasks(AsyncTaskCompletionCallback callback, Activity activity, String field) {
            this.songsUtils = new SongsUtils(activity);
            this.callback = callback;
            this.field = field;
        }
        @Nullable
        @Override
        protected Long doInBackground(String... params) {

            // -- Checking empty playlist or broken songs
            if (isInteger(field)) {

                int playlistID = Integer.parseInt(field);
                ArrayList<SongModel> playListSongs =
                        songsUtils.playlistSongs(playlistID);

                if (!playListSongs.isEmpty()) {
                    for (int j = 0; j < playListSongs.size(); j++) {
                        if (isCancelled()) break;
                        if (!songsUtils.allSongs().contains(playListSongs.get(j))) {
                            boolean isFound = false;
                            for (int k = 0; k < songsUtils.allSongs().size(); k++) {
                                if ((songsUtils.allSongs().get(k).getTitle() +
                                        songsUtils.allSongs().get(k).getDuration())
                                        .equals(playListSongs.get(j).getTitle() +
                                                playListSongs.get(j).getDuration())) {
                                    playListSongs.remove(j);
                                    playListSongs.add(j, songsUtils.allSongs().get(k));
                                    isFound = true;
                                    k = songsUtils.allSongs().size();
                                }
                                if (isCancelled()) {
                                    break; // REMOVE IF NOT USED IN A FOR LOOP
                                }
                            }
                            if (!isFound) {

                                playListSongs.remove(j);
                                j--;
                            }
                        } else {
                        }
                        if (isCancelled()) {
                            break; // REMOVE IF NOT USED IN A FOR LOOP
                        }
                    }
                    // Update favourite songs list
                    songsUtils.updatePlaylistSongs(playlistID,
                            playListSongs);
                }
            } else if (field.equals("favourites")) {

                ArrayList<SongModel> favSongs =
                        new ArrayList<>(songsUtils.favouriteSongs());
                if (!favSongs.isEmpty()) {
                    for (int j = 0; j < favSongs.size(); j++) {
                        if (!songsUtils.allSongs().contains(favSongs.get(j))) {
                            boolean isFound = false;
                            for (int i = 0; i < songsUtils.allSongs().size(); i++) {
                                if ((songsUtils.allSongs().get(i).getTitle() +
                                        songsUtils.allSongs().get(i).getDuration())
                                        .equals(favSongs.get(j).getTitle() +
                                                favSongs.get(j).getDuration())) {
                                    favSongs.remove(j);
                                    favSongs.add(j, songsUtils.allSongs().get(i));
                                    isFound = true;
                                    i = songsUtils.allSongs().size();
                                }
                                if (isCancelled()) break;
                            }
                            if (!isFound) {
                                favSongs.remove(j);
                                j--;
                            }
                        }
                        if (isCancelled()) {
                            break;
                        }
                    }
                    songsUtils.updateFavouritesList(favSongs);
                }
            } else if (field.equals("mostplayed")) {

                // -- Checking Most Played
                ArrayList<SongModel> mostPlayed =
                        songsUtils.mostPlayedSongs();
                if (!mostPlayed.isEmpty()) {
                    for (int j = 0; j < mostPlayed.size(); j++) {
                        if (!songsUtils.allSongs().contains(mostPlayed.get(j))) {
                            boolean isFound = false;
                            for (int i = 0; i < songsUtils.allSongs().size(); i++) {
                                if ((songsUtils.allSongs().get(i).getTitle() +
                                        songsUtils.allSongs().get(i).getDuration())
                                        .equals(mostPlayed.get(j).getTitle() +
                                                mostPlayed.get(j).getDuration())) {
                                    mostPlayed.remove(j);
                                    mostPlayed.add(j, songsUtils.allSongs().get(i));
                                    isFound = true;
                                    i = songsUtils.allSongs().size();
                                    if (isCancelled()) break;
                                }
                            }
                            if (!isFound) {
                                mostPlayed.remove(j);
                                j--;
                            }
                        }
                        if (isCancelled()) {
                            break;
                        }
                    }

                    songsUtils.updateMostPlayedList(mostPlayed);
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }

        @Override
        protected void onPostExecute(Long aLong) {
            callback.updateViews();
        }

        private boolean isInteger(@Nullable String str) {
            if (str == null) {
                return false;
            }
            int length = str.length();
            if (length == 0) {
                return false;
            }
            int i = 0;
            if (str.charAt(0) == '-') {
                if (length == 1) {
                    return false;
                }
                i = 1;
            }
            for (; i < length; i++) {
                char c = str.charAt(i);
                if (c < '0' || c > '9') {
                    return false;
                }
            }
            return true;
        }
    }
}
