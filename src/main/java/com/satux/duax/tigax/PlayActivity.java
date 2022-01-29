package com.satux.duax.tigax;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.satux.duax.tigax.ads.Ads;
import com.satux.duax.tigax.database.FavouriteList;
import com.satux.duax.tigax.database.PlaylistSongs;
import com.satux.duax.tigax.fragments.ImageDetailFragment;
import com.satux.duax.tigax.fragments.QueueFragment;
import com.satux.duax.tigax.models.SongModel;
import com.satux.duax.tigax.services.MusicService;
import com.satux.duax.tigax.utils.CommonUtils;
import com.satux.duax.tigax.utils.Config;
import com.satux.duax.tigax.utils.SharedPrefsUtils;
import com.satux.duax.tigax.utils.SongsUtils;
import com.satux.duax.tigax.zlyric.AdapterLyrics;
import com.satux.duax.tigax.zlyric.HelperLyrics;
import com.xgc1986.parallaxPagerTransformer.ParallaxPagerTransformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PlayActivity extends AppCompatActivity implements OnClickListener, QueueFragment.MyFragmentCallbackOne, Config {
    private final ScheduledExecutorService mExecutorService =
            Executors.newSingleThreadScheduledExecutor();
    private final Handler mHandler = new Handler();
    public int mls;
    int accentColor;
    SharedPrefsUtils sharedPrefsUtils;
    private FrameLayout adContainerView;
    private ImageView btnPlay;
    private ImageView btnNext;
    private ImageView btnPrev, imgFav;
    private TextView title, artist, lyric;
    private ImageView btnRepeat;
    private ViewPager albumArtViewpager;
    private SeekBar seek_bar;
    private View queueFragment;
    private boolean isFavourite;
    private SongsUtils songsUtils;
    private ScrollView lrc_scrolview;
    private RecyclerView lrc_recycleview;
    private ImagePagerAdapter albumArtAdapter;
    @NonNull
    private String TAG = "PlayActivityConsole";
    @Nullable
    private MediaBrowserCompat mMediaBrowser;
    @Nullable
    private PlaybackStateCompat mLastPlaybackState;
    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };
    private ScheduledFuture<?> mScheduleFuture;
    private boolean isDestroy;
    private boolean isTimeLyrics;
    @Nullable
    private AdapterLyrics adapterLyrics;
    private LinearSnapHelper snapHelper;
    private LinearLayoutManager linearLayoutManager;
    private final MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            updatePlaybackState(state);
        }

        @Override
        public void onMetadataChanged(@Nullable MediaMetadataCompat metadata) {
            if (metadata != null) {
                updateMediaDescription(metadata);
                updateDuration(metadata);
            }
        }
    };
    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    try {
                        connectToSession(mMediaBrowser.getSessionToken());
                        ContextCompat.startForegroundService(PlayActivity.this,
                                new Intent(PlayActivity.this, MusicService.class));
                    } catch (RemoteException e) {
                    }
                }
            };
    private int point;
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        Ads.showBanners(PlayActivity.this,this.<LinearLayout>findViewById(R.id.ads_container));


        sharedPrefsUtils = new SharedPrefsUtils(this);
        songsUtils = new SongsUtils(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }

        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicService.class), mConnectionCallback, null);

        /*
         * Getting View Elements
         */
        title = findViewById(R.id.title);
        artist = findViewById(R.id.artist);
        lyric = findViewById(R.id.tx_lyric);
        btnPlay = findViewById(R.id.play);
        btnNext = findViewById(R.id.next);
        btnPrev = findViewById(R.id.prev);
        albumArtViewpager = findViewById(R.id.albumArt);
        seek_bar = findViewById(R.id.seekBar1);
        btnRepeat = findViewById(R.id.repeat);
        imgFav = findViewById(R.id.imageFav);
        queueFragment = findViewById(R.id.fragment);

        title.setSelected(true);

        /*
         * Getting and Setting accent color chosen by user
         */
        accentColor = (new CommonUtils(this).accentColor(sharedPrefsUtils));

        (findViewById(R.id.play_bg)).setBackgroundResource(accentColor);
        (findViewById(R.id.play_activity_bg)).setBackgroundResource(accentColor);

        (findViewById(R.id.play_bg)).setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                if (view.getHeight() < 120) {
                    outline.setRoundRect(0, 0, view.getWidth(), Math.round(view.getHeight()), 50F);
                } else if (view.getHeight() >= 120 && view.getHeight() <= 160) {
                    outline.setRoundRect(0, 0, view.getWidth(), Math.round(view.getHeight()), 75F);
                } else if (view.getHeight() > 160) {
                    outline.setRoundRect(0, 0, view.getWidth(), Math.round(view.getHeight()), 100F);
                }
            }
        });
        (findViewById(R.id.play_bg)).setClipToOutline(true);
        LayerDrawable progressBarDrawable = (LayerDrawable) seek_bar.getProgressDrawable();
        Drawable progressDrawable = progressBarDrawable.getDrawable(1);
        progressDrawable.setColorFilter(ContextCompat.getColor(this, accentColor),
                PorterDuff.Mode.SRC_IN);

        /*
         * Album Art Viewpager
         */

        albumArtAdapter = new ImagePagerAdapter(getSupportFragmentManager(), songsUtils.queue().size());
        albumArtViewpager.setPageTransformer(false, new ParallaxPagerTransformer(R.id.imageView));
        albumArtViewpager.setAdapter(albumArtAdapter);
        albumArtViewpager.setCurrentItem(sharedPrefsUtils.readSharedPrefsInt("musicID", 0));
        albumArtViewpager.setOffscreenPageLimit(3);

        albumArtViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position < 0 || position > songsUtils.queue().size() - 1) {
                    finish();
                    startActivity(getIntent());
                    return;
                }
                if (songsUtils.getCurrentMusicID() < position) {
                    MediaControllerCompat.getMediaController(PlayActivity.this).getTransportControls().skipToNext();
                } else if (songsUtils.getCurrentMusicID() > position) {
                    MediaControllerCompat.getMediaController(PlayActivity.this).getTransportControls().skipToPrevious();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        /*
         * SeekBar Change
         */
        seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                mls = arg1;

                if (isTimeLyrics) {
                    adapterLyrics.notifyDataSetChanged();
                    linearLayoutManager.scrollToPositionWithOffset((getPoint() - 2), 20);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopSeekbarUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MediaControllerCompat.getMediaController(PlayActivity.this).getTransportControls().seekTo(seekBar.getProgress());
                scheduleSeekbarUpdate();
            }
        });

        /*
         * Setting Buttons
         */
        btnPlay.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
        btnRepeat.setOnClickListener(this);
        imgFav.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doItMyFav();
            }
        });
        findViewById(R.id.addToPlayListImageView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                songsUtils.addToPlaylist(songsUtils.queue().get(sharedPrefsUtils.readSharedPrefsInt("musicID", 0)));
            }
        });
        findViewById(R.id.viewQueue).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getResources().getBoolean(R.bool.isLandscape)) {
                    if (queueFragment.getVisibility() == View.VISIBLE) {
                        queueFragment.setVisibility(View.GONE);
                    } else {
                        queueFragment.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        findViewById(R.id.shuffleImageView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                shuffle();
            }
        });
        findViewById(R.id.equalizer).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Ads ads= new Ads(PlayActivity.this);
                ads.setCustomObjectListener(new Ads.MyCustomObjectListener() {
                    @Override
                    public void onAdsfinish() {
                        startActivity(new Intent(PlayActivity.this, EqualizerActivity.class));

                    }
                });
                ads.showInters();

            }
        });
        findViewById(R.id.infoImageView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                songsUtils.info(
                        songsUtils.queue().get(sharedPrefsUtils.readSharedPrefsInt("musicID", 0))
                ).show();
            }
        });
        findViewById(R.id.sleepTimerImageView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Ads ads= new Ads(PlayActivity.this);
                ads.setCustomObjectListener(new Ads.MyCustomObjectListener() {
                    @Override
                    public void onAdsfinish() {
                        startActivity(new Intent(PlayActivity.this, TimerActivity.class));

                    }
                });
                ads.showInters();
            }
        });
        findViewById(R.id.rateImageView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                rateApps();
            }
        });
        findViewById(R.id.shareImageView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                shareApp();
            }
        });
        /*
         * Hiding Songs Queue
         */
        queueFragment.setVisibility(View.GONE);

        lrc_scrolview = findViewById(R.id.lrc_scrollView);
        lrc_scrolview.setVisibility(View.GONE);
        lrc_recycleview = findViewById(R.id.lrc_recycleview);
        lrc_recycleview.setVisibility(View.GONE);
    }
    public int getPoint() {
        int p = point;
        return p;
    }
    public void setPoint(int p) {
        this.point = p;
    }
    public String getDurationTimer() {
        final long minutes = (mls / 1000) / 60;
        final int seconds = (int) ((mls / 1000) % 60);
        String tx = minutes + ":" + seconds;
        return tx;
    }
    @Override
    public void viewPagerRefreshOne() {
        albumArtAdapter = new ImagePagerAdapter(getSupportFragmentManager(), songsUtils.queue().size());
        albumArtViewpager.setAdapter(albumArtAdapter);
        albumArtViewpager.setCurrentItem(sharedPrefsUtils.readSharedPrefsInt("musicID", 0));
    }
    public void setGraphics() {
        /*
         * Favorite and Repeat if enabled or not
         */
        if (getIndex(sharedPrefsUtils.readSharedPrefsString("raw_path", null)) != -1) {
            imgFav.setColorFilter(ContextCompat.getColor(this, accentColor));
            isFavourite = true;
        } else {
            imgFav.setColorFilter(null);
            isFavourite = false;
        }
        if (sharedPrefsUtils.readSharedPrefsBoolean("repeat", false)) {
            btnRepeat.setColorFilter(ContextCompat.getColor(this, accentColor));
        } else {
            btnRepeat.setColorFilter(null);
            sharedPrefsUtils.writeSharedPrefs("repeat", false);
        }

        /*
         * Setting current item in Album Art ViewPager
         */
        if (albumArtViewpager.getCurrentItem() != sharedPrefsUtils.readSharedPrefsInt("musicID", 0)) {
            albumArtViewpager.setVisibility(View.VISIBLE);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            albumArtViewpager.setCurrentItem(sharedPrefsUtils.readSharedPrefsInt("musicID", 0));
                        }
                    });
                }
            });
            thread.start();
        }
    }
    public void shuffle() {
        // Current playing music ID
        int musicID = sharedPrefsUtils.readSharedPrefsInt("musicID", 0);
        // New ArrayList which is an exact copy of current playing queue
        ArrayList<SongModel> arrayList = new ArrayList<>(songsUtils.queue());
        // Current song we are playing
        SongModel currentSong = arrayList.get(musicID);

        // Putting current song on top of list and shuffling remaining songs
        arrayList.remove(musicID);
        Collections.shuffle(arrayList);
        arrayList.add(0, currentSong);

        // Saving the current queue and musicID
        songsUtils.replaceQueue(arrayList);
        sharedPrefsUtils.writeSharedPrefs("musicID", 0);

        // Updating ViewPager and Queue in accordance to new songs queue
        viewPagerRefreshOne();
        QueueFragment queueFragment = (QueueFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (queueFragment != null) {
            queueFragment.notifyFragmentQueueUpdate();
        }

        // Letting user know
        (new CommonUtils(PlayActivity.this)).showTheToast("Shuffling the list");
    }
    private int getIndex(String rawPath) {
        FavouriteList db = new FavouriteList(this);
        db.open();
        ArrayList<SongModel> list;
        list = db.getAllRows();
        db.close();
        int i = 0;
        while (i < list.size()) {
            if (list.get(i).getPath().equals(rawPath)) {
                return i;
            }
            i++;
        }
        return -1;
    }
    private int getIndex(String rawPath, ArrayList<SongModel> list) {
        int i = 0;
        while (i < list.size()) {
            if (list.get(i).getPath().equals(rawPath)) {
                return i;
            }
            i++;
        }
        return -1;
    }
    private void doItMyFav() {

        FavouriteList db = new FavouriteList(PlayActivity.this);
        db.open();
        if (!isFavourite) {
            if (getIndex(sharedPrefsUtils.readSharedPrefsString("raw_path", null)) == -1) {
                SongModel hashMap = songsUtils.queue().get(sharedPrefsUtils.readSharedPrefsInt("musicID", 0));
                if (songsUtils.addToFavouriteSongs(hashMap)) {

                    imgFav.setColorFilter(ContextCompat.getColor(this, accentColor));
                    isFavourite = true;
                    (new CommonUtils(this)).showTheToast("Favourite Added!");
                } else {
                    (new CommonUtils(this)).showTheToast("Unable to add to Favourite!");
                }
            }
        } else {
            ArrayList<SongModel> list = songsUtils.favouriteSongs();
            if (db.deleteAll()) {

                imgFav.setColorFilter(null);

                int n = getIndex(sharedPrefsUtils.readSharedPrefsString("raw_path", null), list);
                list.remove(n);

                for (int i = 0; i < list.size(); i++) {
                    db.addRow(list.get(i));
                }
                isFavourite = false;
                (new CommonUtils(this)).showTheToast("Favourite Removed!");
            } else {
                (new CommonUtils(this)).showTheToast("Unable to Remove Favourite!");
            }
        }
        db.close();
    }
    @Override
    public void onClick(View target) {
        if (target == btnPlay) {
            MediaControllerCompat.getMediaController(PlayActivity.this).getTransportControls().play();
        } else if (target == btnRepeat) {
            if (sharedPrefsUtils.readSharedPrefsBoolean("repeat", false)) {
                btnRepeat.clearColorFilter();
                (new CommonUtils(this)).showTheToast("Repeat Off");
                sharedPrefsUtils.writeSharedPrefs("repeat", false);
            } else {
                btnRepeat.setColorFilter(ContextCompat.getColor(this, accentColor));
                (new CommonUtils(this)).showTheToast("Repeat On");
                sharedPrefsUtils.writeSharedPrefs("repeat", true);
            }
        } else if (target == btnNext) {
            MediaControllerCompat.getMediaController(PlayActivity.this).getTransportControls().skipToNext();
        } else if (target == btnPrev) {
            MediaControllerCompat.getMediaController(PlayActivity.this).getTransportControls().skipToPrevious();
        }
    }
    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(
                PlayActivity.this, token);
        if (mediaController.getMetadata() == null) {
            finish();
            return;
        }
        MediaControllerCompat.setMediaController(this, mediaController);
        mediaController.registerCallback(mCallback);
        PlaybackStateCompat state = mediaController.getPlaybackState();
        updatePlaybackState(state);
        MediaMetadataCompat metadata = mediaController.getMetadata();
        if (metadata != null) {
            updateMediaDescription(metadata);
            updateDuration(metadata);
        }
        updateProgress();
        if (state != null && (state.getState() == PlaybackStateCompat.STATE_PLAYING ||
                state.getState() == PlaybackStateCompat.STATE_BUFFERING)) {
            scheduleSeekbarUpdate();
        }
    }
    private void updateMediaDescription(MediaMetadataCompat metadata) {
        title.setText(metadata.getText(MediaMetadataCompat.METADATA_KEY_TITLE));
        artist.setText(metadata.getText(MediaMetadataCompat.METADATA_KEY_ARTIST));
        String zlyric = String.valueOf(metadata.getText(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION));
        if (zlyric.startsWith("#")) {
            isTimeLyrics = true;
            lrc_scrolview.setVisibility(View.GONE);
            lrc_recycleview.setVisibility(View.VISIBLE);
            showAnimateLyrics(zlyric);
        } else {
            isTimeLyrics = false;
            lrc_recycleview.setVisibility(View.GONE);
            lrc_scrolview.setVisibility(View.VISIBLE);
            lyric.setText(Html.fromHtml(zlyric));
        }

        QueueFragment queueFragment = (QueueFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (queueFragment != null) {
            queueFragment.notifyFragmentQueueUpdate();
        }
        setGraphics();
    }
    private void showAnimateLyrics(String zlyric) {
        snapHelper = new LinearSnapHelper();
        lrc_recycleview.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(lrc_recycleview);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        lrc_recycleview.setLayoutManager(linearLayoutManager);
        adapterLyrics = new AdapterLyrics(this, HelperLyrics.retrieveShoutcasts(zlyric));
        lrc_recycleview.setAdapter(adapterLyrics);
    }
    private void scheduleSeekbarUpdate() {
        stopSeekbarUpdate();
        if (!mExecutorService.isShutdown()) {
            long PROGRESS_UPDATE_INTERNAL = 1000;
            long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(mUpdateProgressTask);
                        }
                    }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
        }
    }
    private void stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        if (mMediaBrowser != null) {
            mMediaBrowser.connect();
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mMediaBrowser != null) {
            mMediaBrowser.disconnect();
        }
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this).unregisterCallback(mCallback);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSeekbarUpdate();
        mExecutorService.shutdown();
    }
    private void updateDuration(@Nullable MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }
        int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        seek_bar.setMax(duration);
    }
    private void updatePlaybackState(@Nullable PlaybackStateCompat state) {
        if (state == null) {
            return;
        }
        mLastPlaybackState = state;

        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                scheduleSeekbarUpdate();
                btnPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.app_pause));
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                stopSeekbarUpdate();
                btnPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.app_play));
                break;
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                stopSeekbarUpdate();
                btnPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.app_play));
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                stopSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_CONNECTING:
                break;
            case PlaybackStateCompat.STATE_ERROR:
                break;
            case PlaybackStateCompat.STATE_FAST_FORWARDING:
                break;
            case PlaybackStateCompat.STATE_REWINDING:
                break;
            case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
                break;
            case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
                break;
            case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM:
                break;
        }
    }
    private void updateProgress() {
        if (mLastPlaybackState == null) {
            return;
        }
        long currentPosition = mLastPlaybackState.getPosition();
        if (mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaControllerCompat.
            long timeDelta = SystemClock.elapsedRealtime() -
                    mLastPlaybackState.getLastPositionUpdateTime();
            currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
        }
        seek_bar.setProgress((int) currentPosition);
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
        finish();
    }
    /*
     * Queue Item Click
     */
    public void onItemClick(int mPosition) {
        if (mPosition >= 0) {
            MediaControllerCompat.getMediaController(this).getTransportControls().skipToQueueItem(mPosition);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.play, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            backPressed();
        } else if (id == R.id.action_drive_mode) {
            Ads ads= new Ads(PlayActivity.this);
            ads.setCustomObjectListener(new Ads.MyCustomObjectListener() {
                @Override
                public void onAdsfinish() {
                    startActivity(new Intent(PlayActivity.this, DriveModeActivity.class));

                }
            });
            ads.showInters();

        } else if (id == R.id.set_suffle) {
            shuffle();
        } else if (id == R.id.add_to_playlist) {
            songsUtils.addToPlaylist(songsUtils.queue().get(sharedPrefsUtils.readSharedPrefsInt("musicID", 0)));
        } else if (id == R.id.equalizer) {
            Ads ads= new Ads(PlayActivity.this);
            ads.setCustomObjectListener(new Ads.MyCustomObjectListener() {
                @Override
                public void onAdsfinish() {
                    startActivity(new Intent(PlayActivity.this, EqualizerActivity.class));

                }
            });
            ads.showInters();

        } else if (id == R.id.save_as_playlist) {
            final Dialog alertDialog = new Dialog(this);
            alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            alertDialog.setContentView(R.layout.dialog_add_playlist);

            final EditText input = alertDialog.findViewById(R.id.editText);
            input.requestFocus();
            input.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, (new CommonUtils(this)).accentColor(sharedPrefsUtils))));

            Button btnCreate = alertDialog.findViewById(R.id.btnCreate);
            btnCreate.setTextColor(ContextCompat.getColor(this, (new CommonUtils(this)).accentColor(sharedPrefsUtils)));

            Button btnCancel = alertDialog.findViewById(R.id.btnCancel);
            btnCancel.setTextColor(ContextCompat.getColor(this, (new CommonUtils(this)).accentColor(sharedPrefsUtils)));

            btnCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = input.getText().toString();
                    if (!name.isEmpty()) {
                        songsUtils.addPlaylist(name);
                        (new CommonUtils(PlayActivity.this)).showTheToast("Adding songs to list: " + name);
                        alertDialog.cancel();
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                PlaylistSongs db = new PlaylistSongs(PlayActivity.this);
                                db.open();
                                ArrayList<SongModel> data = songsUtils.queue();
                                int playlistID = Integer.parseInt(Objects.requireNonNull(songsUtils.getAllPlaylists().get(
                                        songsUtils.getAllPlaylists().size() - 1).get("ID")));
                                for (int i = 0; i < data.size(); i++) {
                                    db.addRow(playlistID, data.get(i));
                                }
                                db.close();
                            }
                        });
                        thread.run();
                    } else {
                        Toast.makeText(PlayActivity.this, "Please enter playlist name.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.cancel();
                }
            });
            alertDialog.show();
        } else if (id == R.id.info) {

            songsUtils.info(
                    songsUtils.queue().get(sharedPrefsUtils.readSharedPrefsInt("musicID", 0))
            ).show();
        } else if (id == R.id.privacy) {
            privacyApp();
        } else if (id == R.id.disclamer) {
            disclaimerApp();
        }
        return super.onOptionsItemSelected(item);
    }
    public void shareApp() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType(TYPE);
        share.putExtra(Intent.EXTRA_TEXT, L_SHARE + getPackageName());
        startActivity(Intent.createChooser(share, SHARE_APP));
    }
    public void rateApps() {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(T_RATE);
        alert.setMessage(P_RATE);
        alert.setPositiveButton(R_RATE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent rate = new Intent(Intent.ACTION_VIEW);
                rate.setData(Uri.parse(L_RATE + getPackageName()));
                startActivity(rate);
            }
        });

        alert.setNegativeButton(X_RATE, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        Dialog mDialog = alert.create();
        mDialog.show();
    }
    private void privacyApp() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(T_PRIVACY);
        alert.setMessage(Html.fromHtml(PRIVACY));

        alert.setNeutralButton(OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        Dialog mDialog = alert.create();
        mDialog.show();
    }
    private void disclaimerApp() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(T_DISCLAIMER);
        alert.setMessage(Html.fromHtml(Config.DISCLAIMER));
        alert.setNeutralButton(OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        Dialog mDialog = alert.create();
        mDialog.show();
    }

    private static class ImagePagerAdapter extends FragmentPagerAdapter {

        private final int mSize;

        ImagePagerAdapter(FragmentManager fm, int size) {
            super(fm);
            mSize = size;
        }

        @Override
        public int getCount() {
            return mSize;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return ImageDetailFragment.newInstance(position);
        }
    }
}
