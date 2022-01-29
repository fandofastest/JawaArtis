package com.satux.duax.tigax.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.satux.duax.tigax.PlayActivity;
import com.satux.duax.tigax.R;
import com.satux.duax.tigax.services.MusicService;
import com.satux.duax.tigax.utils.CommonUtils;
import com.satux.duax.tigax.utils.SharedPrefsUtils;
import com.satux.duax.tigax.utils.SongsUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.support.v4.media.session.PlaybackStateCompat.STATE_BUFFERING;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_CONNECTING;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_ERROR;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_FAST_FORWARDING;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_NONE;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_REWINDING;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_SKIPPING_TO_NEXT;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_STOPPED;

public class MusicDockFragment extends Fragment {

    private final ScheduledExecutorService mExecutorService =
            Executors.newSingleThreadScheduledExecutor();
    private final Handler mHandler = new Handler();
    @Nullable
    PlaybackStateCompat mLastPlaybackState = null;
    private TextView title;
    private TextView artist;
    private ImageView btnPlay;
    private ProgressBar progressBar;
    private ProgressBar spiNer;
    @Nullable
    private MediaBrowserCompat mMediaBrowser;
    @Nullable
    private SongsUtils songsUtils;
    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };
    private ScheduledFuture<?> mScheduleFuture;
    private final MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            updatePlaybackState(state);
            updateProgress();
        }

        @Override
        public void onMetadataChanged(@Nullable MediaMetadataCompat metadata) {
            if (metadata != null) {
                updateMediaDescription(metadata);
            }
        }
    };
    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    try {
                        connectToSession(mMediaBrowser.getSessionToken());
                        ContextCompat.startForegroundService(getActivity(), new Intent(getActivity(), MusicService.class));
                    } catch (RemoteException e) {
                    }
                }
            };
    @NonNull
    private String TAG = "MusicDockConsole";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music_dock, container,
                false);
        title = view.findViewById(R.id.XtextView1);
        artist = view.findViewById(R.id.XtextView2);
        btnPlay = view.findViewById(R.id.XbtnPlay);

        progressBar = view.findViewById(R.id.progressBar);
        spiNer = view.findViewById(R.id.spinner);
        final Button btnPlayActivity = view.findViewById(R.id.Xbutton1);
        title.setSelected(true);

        LayerDrawable progressBarDrawable = (LayerDrawable) progressBar.getProgressDrawable();
        Drawable progressDrawable = progressBarDrawable.getDrawable(1);
        progressDrawable.setColorFilter(ContextCompat.getColor(getActivity(), (new CommonUtils(getActivity())
                        .accentColor(new SharedPrefsUtils(getActivity())))),
                PorterDuff.Mode.SRC_IN);

        songsUtils = new SongsUtils(getActivity());

        btnPlayActivity.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                touchDock();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!songsUtils.queue().isEmpty()) {
                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls().play();
                }
            }
        });

        mMediaBrowser = new MediaBrowserCompat(getActivity(),
                new ComponentName(getActivity(), MusicService.class), mConnectionCallback, null);

        if (mMediaBrowser != null) {
            mMediaBrowser.connect();
        }

        return view;
    }
    private void touchDock() {
        if (!songsUtils.queue().isEmpty()) {

            Intent intent = new Intent(getActivity(), PlayActivity.class);
            startActivity(intent);
        } else {
            (new CommonUtils(getActivity())).showTheToast("No music found in device, try Sync in options.");
        }
    }
    private void updateMediaDescription(MediaMetadataCompat metadata) {
        title.setText(metadata.getText(MediaMetadataCompat.METADATA_KEY_TITLE));
        artist.setText(metadata.getText(MediaMetadataCompat.METADATA_KEY_ARTIST));
    }
    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(
                getActivity(), token);
        if (mediaController.getMetadata() == null) {
            return;
        }
        MediaControllerCompat.setMediaController(getActivity(), mediaController);
        mediaController.registerCallback(mCallback);
        PlaybackStateCompat state = mediaController.getPlaybackState();
        updateMediaDescription(mediaController.getMetadata());
        updatePlaybackState(state);
        MediaMetadataCompat metadata = mediaController.getMetadata();
        if (metadata != null) {
            updateMediaDescription(metadata);
        }
        updateDuration(metadata);
        updateProgress();
        if (state != null && (state.getState() == STATE_PLAYING || state.getState() == STATE_BUFFERING)) {
            scheduleSeekbarUpdate();
        }
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
    private void updateDuration(@Nullable MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }
        if (mLastPlaybackState == null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils(getActivity());
            if (sharedPrefsUtils.readSharedPrefsString("raw_path", "").equals(songsUtils.queue().get(songsUtils.getCurrentMusicID()).getPath())) {
                progressBar.setMax(sharedPrefsUtils.readSharedPrefsInt("durationInMS", 0));
            }
            return;
        }
        int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        progressBar.setMax(duration);
    }
    private void updateProgress() {
        if (mLastPlaybackState == null) {
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils(getActivity());
            if (sharedPrefsUtils.readSharedPrefsString("raw_path", "").equals(songsUtils.queue().get(songsUtils.getCurrentMusicID()).getPath())) {
                progressBar.setProgress(sharedPrefsUtils.readSharedPrefsInt("song_position", 0));
            }
            return;
        }
        long currentPosition = mLastPlaybackState.getPosition();
        if (mLastPlaybackState.getState() == STATE_PLAYING) {
            long timeDelta = SystemClock.elapsedRealtime() -
                    mLastPlaybackState.getLastPositionUpdateTime();
            currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
        }
        progressBar.setProgress((int) currentPosition);
    }
    private void stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    public void onStop() {
        super.onStop();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSeekbarUpdate();
        mExecutorService.shutdown();
    }
    private void updatePlaybackState(@Nullable PlaybackStateCompat state) {
        if (state == null) {
            return;
        }
        mLastPlaybackState = state;

        switch (state.getState()) {
            case STATE_PLAYING:
                scheduleSeekbarUpdate();
                spiNer.setVisibility(View.GONE);
                btnPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.app_pause));
                break;
            case STATE_PAUSED:
                stopSeekbarUpdate();
                btnPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.app_play));
                break;
            case STATE_NONE:
                btnPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.app_play));
                break;
            case STATE_STOPPED:
                stopSeekbarUpdate();
                btnPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.app_play));

                break;
            case STATE_BUFFERING:
                stopSeekbarUpdate();
                spiNer.setVisibility(View.VISIBLE);
                break;
            default:
            case STATE_CONNECTING:
                break;
            case STATE_ERROR:
                break;
            case STATE_FAST_FORWARDING:
                break;
            case STATE_REWINDING:
                break;
            case STATE_SKIPPING_TO_NEXT:
                break;
            case STATE_SKIPPING_TO_PREVIOUS:
                break;
            case STATE_SKIPPING_TO_QUEUE_ITEM:
                break;
        }
    }
}

