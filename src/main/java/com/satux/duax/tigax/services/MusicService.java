package com.satux.duax.tigax.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import androidx.media.session.MediaButtonReceiver;

import com.satux.duax.tigax.MainActivity;
import com.satux.duax.tigax.R;
import com.satux.duax.tigax.database.CategorySongs;
import com.satux.duax.tigax.helpers.MediaStyleHelper;
import com.satux.duax.tigax.utils.CommonUtils;
import com.satux.duax.tigax.utils.SharedPrefsUtils;
import com.satux.duax.tigax.utils.SongsUtils;
import com.satux.duax.tigax.widgets.MusicWidget4x1;
import com.satux.duax.tigax.widgets.MusicWidget4x1v2;
import com.satux.duax.tigax.widgets.MusicWidget4x2;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.List;

public class MusicService extends MediaBrowserServiceCompat implements
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnPreparedListener {

    /******* ---------------------------------------------------------------
     Public Static
     ----------------------------------------------------------------*******/

    public static final String ACTION_CLOSE = "com.satux.duax.tigax.action.CLOSE";
    public static final String ACTION_PLAY = "com.satux.duax.tigax.action.PLAY";
    public static final String ACTION_PLAY_PAUSE = "com.satux.duax.tigax.action.PLAY_PAUSE";
    public static final String ACTION_TRACK_PREV = "com.satux.duax.tigax.action.TRACK_PREV";
    public static final String ACTION_TRACK_NEXT = "com.satux.duax.tigax.action.TRACK_NEXT";
    public static final String ACTION_REPEAT = "com.satux.duax.tigax.action.REPEAT";
    public static final String ACTION_PERSISTENT_NOTIFICATION = "com.satux.duax.tigax.action.PERSISTENT_NOTIFICATION";

    /******* ---------------------------------------------------------------
     Private
     ----------------------------------------------------------------*******/

    private final String TAG = "PlaybackServiceConsole";
    private final int NOTIFICATION_ID = 614436745;
    /******* ---------------------------------------------------------------
     AudioFocus
     ----------------------------------------------------------------*******/
    boolean autoPaused = false;
    Equalizer eq;
    BassBoost bassBoost;
    Virtualizer virtualizer;
    private MediaPlayer mMediaPlayer;
    @Nullable
    private MediaSessionCompat mMediaSessionCompat;
    private PlaybackStateCompat.Builder mPlaybackStateBuilder;
    private SharedPrefsUtils sharedPrefsUtils;
    private SongsUtils songsUtils;
    private MediaPlayer mMediaPlayer2;
    private int currentMediaPlayer = 0; // 0 - mMediaPlayer; 1 - mMediaPlayer2
    private int crossfadeDuration = 3000; // 3 seconds
    private Handler mHandler;
    // Volume
    private float mFadeOutVolume = 1.0f;
    private float mFadeInVolume = 0.0f;
    @NonNull
    public Runnable crossFadeRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                int crossfadeStep = 10000; // 10 seconds before finishing and into next song

                if (currentMediaPlayer == 0) {
                    // mMediaPlayer

                    mMediaPlayer.setVolume(mFadeInVolume, mFadeInVolume);
                    mMediaPlayer2.setVolume(mFadeOutVolume, mFadeOutVolume);

                    // Start next track and run Crossfade
                    mMediaPlayer.seekTo(crossfadeStep);
                    mMediaPlayer.start();
                } else {
                    // mMediaPlayer2

                    mMediaPlayer2.setVolume(mFadeInVolume, mFadeInVolume);
                    mMediaPlayer.setVolume(mFadeOutVolume, mFadeOutVolume);

                    // Start next track and run Crossfade
                    mMediaPlayer2.seekTo(crossfadeStep);
                    mMediaPlayer2.start();
                }

                mFadeInVolume = mFadeInVolume + (1.0f / (((float) crossfadeDuration / 1000) * 10.0f));
                mFadeOutVolume = mFadeOutVolume - (1.0f / (((float) crossfadeDuration / 1000) * 10.0f));

                mHandler.postDelayed(crossFadeRunnable, 100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    // Detects and starts cross-fading
    @NonNull
    public Runnable detectCrossFadeRunnable = new Runnable() {
        @Override
        public void run() {
            //Check if we're in the last part of the current song.
            try {
                if (getCurrentMediaPlayer().isPlaying()) {
                    if (getCurrentMediaPlayer().getCurrentPosition() >=
                            (getCurrentMediaPlayer().getDuration() - crossfadeDuration)) {
                        // Start cross-fading the songs
                        switchMediaPlayer();
                        mHandler.postDelayed(crossFadeRunnable, 100);
                    } else {
                        mHandler.postDelayed(detectCrossFadeRunnable, 1000);
                    }
                } else {
                    mHandler.postDelayed(detectCrossFadeRunnable, 1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    /******* ---------------------------------------------------------------
     MediaSessionCompat Methods
     ----------------------------------------------------------------*******/
    @NonNull
    private MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);
        }

        @Override
        public void onPlay() {
            super.onPlay();
            processPlayPause();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            processNextRequest();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            processPrevRequest();
        }

        @Override
        public void onRewind() {
            super.onRewind();
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            super.onSetRepeatMode(repeatMode);
        }

        @Override
        public void onSkipToQueueItem(long id) {
            super.onSkipToQueueItem(id);
            doPushPlay((int) id);
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            getCurrentMediaPlayer().seekTo((int) (long) pos);
            setMediaPlaybackState(mPlaybackStateBuilder.build().getState());
        }
    };
    @NonNull
    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getCurrentMediaPlayer() != null && getCurrentMediaPlayer().isPlaying()) {
                processPauseRequest();
            }
        }
    };
    /******* ---------------------------------------------------------------
     Service Methods and Intents
     ----------------------------------------------------------------*******/

    public MusicService() {
        super();
    }
    @Override
    public void onCreate() {
        super.onCreate();

        sharedPrefsUtils = new SharedPrefsUtils(this);
        songsUtils = new SongsUtils(this);
        mHandler = new Handler();

        /*
         Initialize
         */
        checkErrorInPrefs();
        initMediaPlayer();
        initMediaSession();
        initNoisyReceiver();

        /*
         * Calling startForeground() under 5 seconds to avoid ANR
         */
        initNotification();
    }
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null) {
            /*
             * Analyze and Acting on the intent received by Service
             * Every request to Service should be with one of the intents in our switch
             */
            MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent);
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_PLAY: {
                        if (mPlaybackStateBuilder.build().getState() == PlaybackStateCompat.STATE_PAUSED) {
                            getCurrentMediaPlayer().start();
                            initNotification();
                            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                        } else {
                            resetMediaPlayerPosition();
                            processPlayRequest();
                        }

                        break;
                    }
                    case ACTION_PLAY_PAUSE: {
                        processPauseRequest();
                        break;
                    }
                    case ACTION_TRACK_PREV: {
                        processPrevRequest();
                        break;
                    }
                    case ACTION_TRACK_NEXT: {
                        processNextRequest();
                        break;
                    }
                    case ACTION_REPEAT: {
                        musicWidgetsReset();
                        break;
                    }
                    case ACTION_CLOSE: {
                        if (!sharedPrefsUtils.readSharedPrefsBoolean("persistentNotificationPref", false)) {
                            processCloseRequest();
                        } else {
                            processPauseRequest();
                        }
                        break;
                    }
                    case ACTION_PERSISTENT_NOTIFICATION:
                        if (mPlaybackStateBuilder.build().getState() != PlaybackStateCompat.STATE_PLAYING) {
                            initNotification();
                        }
                        break;
                    default: {
                        initNotification();
                    }
                }
            } else {
                initNotification();
            }
        }
        return START_STICKY;
    }
    /*
     * Getting and Switching MediaPlayers for Cross-fade
     */
    private MediaPlayer getCurrentMediaPlayer() {
        return (currentMediaPlayer == 0) ? mMediaPlayer : mMediaPlayer2;
    }
    private void checkErrorInPrefs() {
        if (songsUtils.getCurrentMusicID() > songsUtils.queue().size() - 1) {
            songsUtils.setCurrentMusicID(0);
        }
    }
    private void switchMediaPlayer() {
        if (currentMediaPlayer == 0) {
            currentMediaPlayer++; // output 1
        } else if (currentMediaPlayer == 1) {
            currentMediaPlayer--; // output 0
        } else {
            currentMediaPlayer = 0;
        }
    }
    private void crossfade() {
        mHandler.post(detectCrossFadeRunnable);
    }
    private void cancelCrossfade() {
        mHandler.removeCallbacks(detectCrossFadeRunnable);
    }
    /*
     * saveData() writes current song parameters to sharedPrefs which can be retrieved in
     * other activities or fragments as well as when we start app next time
     * musicID: is id of current item in queue
     * title, artist, album, albumid: are all fields of SongModel()
     *
     */
    private void saveData() {
        int musicID = songsUtils.getCurrentMusicID();
        sharedPrefsUtils.writeSharedPrefs("audio_session_id", getCurrentMediaPlayer().getAudioSessionId());
        try {
            sharedPrefsUtils.writeSharedPrefs("musicID", musicID);
            sharedPrefsUtils.writeSharedPrefs("title", songsUtils.queue().get(musicID).getTitle());
            sharedPrefsUtils.writeSharedPrefs("artist", songsUtils.queue().get(musicID).getArtist());
            sharedPrefsUtils.writeSharedPrefs("lyric", songsUtils.queue().get(musicID).getLyric());
            sharedPrefsUtils.writeSharedPrefs("album", songsUtils.queue().get(musicID).getAlbum());
            sharedPrefsUtils.writeSharedPrefs("albumid", songsUtils.queue().get(musicID).getAlbumID());
            sharedPrefsUtils.writeSharedPrefs("raw_path", songsUtils.queue().get(musicID).getPath());
            sharedPrefsUtils.writeSharedPrefs("duration", songsUtils.queue().get(musicID).getDuration());
            sharedPrefsUtils.writeSharedPrefs("durationInMS", getCurrentMediaPlayer().getDuration());
        } catch (Exception e) {
        }
    }
    private void doPushPlay(int id) {
        songsUtils.setCurrentMusicID(id);
        getCurrentMediaPlayer().reset();
        if (successfullyRetrievedAudioFocus()) {
            initNotification();
            return;
        }
        setMediaPlayer(songsUtils.queue().get(id).getPath());
    }
    private void processNextRequest() {
        resetMediaPlayerPosition();

        int musicID = songsUtils.getCurrentMusicID();

        if (musicID + 1 != songsUtils.queue().size()) {
            musicID++;
        } else {
            musicID = 0;
        }
        songsUtils.setCurrentMusicID(musicID);
        if (successfullyRetrievedAudioFocus()) {
            initNotification();
            return;
        }

        setMediaPlayer(songsUtils.queue().get(musicID).getPath());
    }
    private void processPrevRequest() {
        resetMediaPlayerPosition();

        if (getCurrentMediaPlayer().getCurrentPosition() < 5000) {
            int musicID = songsUtils.getCurrentMusicID();
            if (musicID > 0) {
                musicID--;
                songsUtils.setCurrentMusicID(musicID);
                if (successfullyRetrievedAudioFocus()) {
                    initNotification();
                    return;
                }
                setMediaPlayer(songsUtils.queue().get(musicID).getPath());
            } else {
                mMediaSessionCompat.getController().getTransportControls().seekTo(0);
            }
        } else {
            mMediaSessionCompat.getController().getTransportControls().seekTo(0);
        }
    }
    private void processCloseRequest() {
        stopSelf();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedPrefsUtils.writeSharedPrefs("song_position", getCurrentMediaPlayer().getCurrentPosition());
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        assert audioManager != null;
        audioManager.abandonAudioFocus(this);
        unregisterReceiver(mNoisyReceiver);
        setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
        mMediaSessionCompat.setActive(false);
        mMediaSessionCompat.release();
        getCurrentMediaPlayer().release();
        try {
            eq.release();
            bassBoost.release();
            virtualizer.release();
        } catch (Exception ignored) {
        }
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
        stopForeground(true);
    }
    private void processPauseRequest() {
        if (mPlaybackStateBuilder.build().getState() == PlaybackStateCompat.STATE_PLAYING) {
            mMediaSessionCompat.setActive(false);
            getCurrentMediaPlayer().pause();
            sharedPrefsUtils.writeSharedPrefs("song_position", getCurrentMediaPlayer().getCurrentPosition());
            setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
            initNotification();
        }
    }
    private void processPlayRequest() {
        if (successfullyRetrievedAudioFocus()) {
            initNotification();
            return;
        }
        setMediaPlaybackState(PlaybackStateCompat.STATE_BUFFERING);
        setMediaPlayer(songsUtils.queue().get(songsUtils.getCurrentMusicID()).getPath());
    }
    private void processPlayPause() {
        if (mPlaybackStateBuilder.build().getState() == PlaybackStateCompat.STATE_PLAYING) {
            processPauseRequest();
        } else if (mPlaybackStateBuilder.build().getState() == PlaybackStateCompat.STATE_PAUSED) {
            if (successfullyRetrievedAudioFocus()) {
                return;
            }
            mMediaSessionCompat.setActive(true);
            getCurrentMediaPlayer().start();
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            initNotification();
            autoPaused = false;
        } else {
            processPlayRequest();
        }
    }
    private void setMetaData() {
        if (songsUtils.queue().size() == 0) {
            MediaMetadataCompat mMediaMetadataCompat = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, sharedPrefsUtils.readSharedPrefsString("title", "Placeholder Title"))
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, sharedPrefsUtils.readSharedPrefsString("album", "Placeholder Album"))
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, sharedPrefsUtils.readSharedPrefsString("lyric", "No Lyric Found"))
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, (long) sharedPrefsUtils.readSharedPrefsInt("durationInMS", 0))
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, grabAlbumArt(sharedPrefsUtils.readSharedPrefsString("albumid", "0")))
                    .build();
            mMediaSessionCompat.setMetadata(mMediaMetadataCompat);
        } else {
            int musicID = songsUtils.getCurrentMusicID();
            MediaMetadataCompat mMediaMetadataCompat = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songsUtils.queue().get(musicID).getTitle())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, songsUtils.queue().get(musicID).getAlbum())
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, sharedPrefsUtils.readSharedPrefsString("lyric", "No Lyric Found"))
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songsUtils.queue().get(musicID).getArtist())
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, (long) sharedPrefsUtils.readSharedPrefsInt("durationInMS", 0))
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, grabAlbumArt(songsUtils.queue().get(musicID).getAlbumID()))
                    .build();
            mMediaSessionCompat.setMetadata(mMediaMetadataCompat);
        }
    }
    private void setGraphics() {
        saveData();
        setMetaData();
    }
    @Nullable
    private Bitmap grabAlbumArt(String albumID) {
        Bitmap art = null;
        try {
            try {
                final Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");

                Uri uri = ContentUris.withAppendedId(sArtworkUri,
                        Long.parseLong(albumID));

                ParcelFileDescriptor pfd = getApplicationContext().getContentResolver()
                        .openFileDescriptor(uri, "r");

                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    art = BitmapFactory.decodeFileDescriptor(fd);
                }
            } catch (Exception ignored) {
            }
            if (art == null) {
                return drawableToBitmap(ContextCompat.getDrawable(this, R.mipmap.ic_launcher));
            } else {
                return art;
            }
        } catch (Exception e) {
            return drawableToBitmap(ContextCompat.getDrawable(MusicService.this,
                    R.mipmap.ic_launcher));
        }
    }
    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 300;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 300;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
    private void setMediaPlaybackState(int state) {
        mPlaybackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY
                | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_STOP
                | PlaybackStateCompat.ACTION_SEEK_TO);

        mPlaybackStateBuilder.setState(state, getCurrentMediaPlayer().getCurrentPosition(), 1.0f, SystemClock.elapsedRealtime());
        if (mPlaybackStateBuilder.build().getState() == PlaybackStateCompat.STATE_PLAYING) {

        } else if (mPlaybackStateBuilder.build().getState() == PlaybackStateCompat.STATE_PAUSED) {

        } else if (mPlaybackStateBuilder.build().getState() == PlaybackStateCompat.STATE_STOPPED) {

        }
        mMediaSessionCompat.setPlaybackState(mPlaybackStateBuilder.build());
    }
    /******* ---------------------------------------------------------------
     Notifications
     ----------------------------------------------------------------*******/
    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager =
                    (NotificationManager) this
                            .getSystemService(Context.NOTIFICATION_SERVICE);
            // The id of the channel.
            String id = "channel_music_playback";
            // The user-visible name of the channel.
            CharSequence name = "Media Playback";
            // The user-visible description of the channel.
            String description = "Media playback controls";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.setShowBadge(false);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }
    private void initNotification() {
        musicWidgetsReset();
        createChannel();
        NotificationCompat.Builder builder
                = MediaStyleHelper.from(MusicService.this, mMediaSessionCompat);

        PendingIntent pCloseIntent = PendingIntent.getService(this, 0,
                (new Intent(this, MusicService.class)).setAction(MusicService.ACTION_CLOSE), 0);

        PendingIntent prevIntent = PendingIntent.getService(this, 0,
                (new Intent(this, MusicService.class)).setAction(MusicService.ACTION_TRACK_PREV), 0);
        PendingIntent playIntent = PendingIntent.getService(this, 0,
                (new Intent(this, MusicService.class)).setAction(MusicService.ACTION_PLAY), 0);
        PendingIntent playPauseIntent = PendingIntent.getService(this, 0,
                (new Intent(this, MusicService.class)).setAction(MusicService.ACTION_PLAY_PAUSE), 0);
        PendingIntent nextIntent = PendingIntent.getService(this, 0,
                (new Intent(this, MusicService.class)).setAction(MusicService.ACTION_TRACK_NEXT), 0);

        builder.addAction(new NotificationCompat.Action(R.drawable.app_previous, "Previous", prevIntent));
        if (getCurrentMediaPlayer().isPlaying()) {
            builder.addAction(new NotificationCompat.Action(R.drawable.app_pause, "Play", playPauseIntent));
        } else {
            builder.addAction(new NotificationCompat.Action(R.drawable.app_play, "Pause", playIntent));
        }
        builder.addAction(new NotificationCompat.Action(R.drawable.app_next, "Next", nextIntent));
        builder.setStyle(new MediaStyle().setShowActionsInCompactView(0).setMediaSession(mMediaSessionCompat.getSessionToken()));
        builder.setSmallIcon(R.drawable.ic_library_music_black_24dp);
        builder.setStyle(new MediaStyle()
                .setShowActionsInCompactView(1, 2).setMediaSession(getSessionToken()));
        builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
        builder.setDeleteIntent(pCloseIntent);
        builder.setColor(ContextCompat.getColor(this, (new CommonUtils(this)).accentColor(sharedPrefsUtils)));
        builder.setShowWhen(false);
        startForeground(NOTIFICATION_ID, builder.build());
        if (!sharedPrefsUtils.readSharedPrefsBoolean("persistentNotificationPref", false)) {
            stopForeground(false);
        }
    }
    /******* ---------------------------------------------------------------
     MediaPlayer
     ----------------------------------------------------------------*******/
    @Override
    public void onCompletion(MediaPlayer mp) {
        getCurrentMediaPlayer().reset();
        resetMediaPlayerPosition();
        if (!sharedPrefsUtils.readSharedPrefsBoolean("repeat", false)) {
            processNextRequest();
        } else {
            setMediaPlayer((songsUtils.queue().get(songsUtils.getCurrentMusicID()).getPath()));
        }
    }
    void resetMediaPlayerPosition() {
        sharedPrefsUtils.writeSharedPrefs("song_position", 0);
    }
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        /*
         * Getting to saved location of song if playback state is none i.e. first instance of music playback
         * , and if we are playing same track we were playing before, if track is new then we won't seek to last
         * remembered location
         * We reset this location to zero when we start playing a new song
         */
        if (mPlaybackStateBuilder.build().getState() == PlaybackStateCompat.STATE_NONE &&
                sharedPrefsUtils.readSharedPrefsString("raw_path", "").equals(songsUtils.queue().get(songsUtils.getCurrentMusicID()).getPath())) {
            mMediaSessionCompat.getController().getTransportControls().seekTo(sharedPrefsUtils.readSharedPrefsInt("song_position", 0));
        }

        /*
         * Setting Equalizer
         */
        setEqualizer();

        /*
         * Setting metaData
         */
        setGraphics();

        mMediaSessionCompat.setActive(true);
        autoPaused = false;
        getCurrentMediaPlayer().start();
        setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
        initNotification();
    }
    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setVolume(1.0f, 1.0f);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer2 = new MediaPlayer();
        mMediaPlayer2.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer2.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer2.setVolume(1.0f, 1.0f);
        mMediaPlayer2.setOnCompletionListener(this);
        mMediaPlayer2.setOnPreparedListener(this);
        mMediaPlayer2.setAudioSessionId(mMediaPlayer.getAudioSessionId());
        sharedPrefsUtils.writeSharedPrefs("audio_session_id", mMediaPlayer.getAudioSessionId());
    }
    private void setMediaPlayer(String path) {
        getCurrentMediaPlayer().reset();
        File file = new File(path);
        if (file.exists()) {
            try {
                addVoteToTrack(path);
                getCurrentMediaPlayer().setDataSource(path);
                getCurrentMediaPlayer().prepare();
            } catch (IOException e) {
                processNextRequest();
                e.printStackTrace();
            }
        } else {
            try {
                if (path.contains("http://")) {
                    addVoteToTrack(path);
                    getCurrentMediaPlayer().setDataSource(path);
                    try {
                        getCurrentMediaPlayer().prepareAsync();
                    } catch (Exception e) {
                        processNextRequest();
                        e.printStackTrace();
                    }
                } else {
                    //AssetFileDescriptor assetFileDescriptor = getApplicationContext().getAssets().openFd(path);
                    addVoteToTrack(path);
                    //getCurrentMediaPlayer().setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                    try {
                        getCurrentMediaPlayer().prepare();
                    } catch (Exception e) {
                        processNextRequest();
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                processNextRequest();
                e.printStackTrace();
            }
        }
    }
    private void initMediaSession() {
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mMediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "Tag", mediaButtonReceiver, null);

        mMediaSessionCompat.setCallback(mMediaSessionCallback);
        mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mMediaSessionCompat.setMediaButtonReceiver(pendingIntent);

        mPlaybackStateBuilder = new PlaybackStateCompat.Builder();

        setSessionToken(mMediaSessionCompat.getSessionToken());

        setMetaData();
    }
    private void initNoisyReceiver() {
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);
    }
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS: {
                if (getCurrentMediaPlayer().isPlaying()) {
                    processPauseRequest();
                    autoPaused = true;
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                if (getCurrentMediaPlayer().isPlaying()) {
                    processPauseRequest();
                    autoPaused = true;
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                if (getCurrentMediaPlayer() != null) {
                    getCurrentMediaPlayer().setVolume(0.3f, 0.3f);
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN: {
                if (getCurrentMediaPlayer() != null) {
                    if (!getCurrentMediaPlayer().isPlaying() && autoPaused) {
                        processPlayPause();
                    }
                    getCurrentMediaPlayer().setVolume(1.0f, 1.0f);
                }
                break;
            }
        }
    }
    private boolean successfullyRetrievedAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        assert audioManager != null;
        int result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_GAIN) {
        }

        return result != AudioManager.AUDIOFOCUS_GAIN;
    }
    private void setEqualizer() {
        boolean isEqInSettings = sharedPrefsUtils.readSharedPrefsBoolean("turnEqualizer", false);
        int currentEqProfile = sharedPrefsUtils.readSharedPrefsInt("currentEqProfile", 0);
        try {
            eq = new Equalizer(0, getCurrentMediaPlayer().getAudioSessionId());
            eq.setEnabled(isEqInSettings);
            for (int i = 0; i < eq.getNumberOfBands(); i++) {
                eq.setBandLevel((short) i, (short) sharedPrefsUtils.readSharedPrefsInt(
                        "profile" + currentEqProfile + "Band" + i, 0));
            }
        } catch (Exception e) {
            (new CommonUtils(this)).showTheToast("Unable to run Equalizer");
        }
        try {
            bassBoost = new BassBoost(0, getCurrentMediaPlayer().getAudioSessionId());
            bassBoost.setEnabled(isEqInSettings);
            bassBoost.setStrength((short) sharedPrefsUtils.readSharedPrefsInt("bassLevel" + currentEqProfile, 0));
        } catch (Exception ignored) {
        }
        try {
            virtualizer = new Virtualizer(0, getCurrentMediaPlayer().getAudioSessionId());
            virtualizer.setEnabled(isEqInSettings);
            virtualizer.setStrength((short) sharedPrefsUtils.readSharedPrefsInt("vzLevel" + currentEqProfile, 0));
        } catch (Exception ignored) {
        }
    }

    void addVoteToTrack(String path) {
        path = path.trim();
        try {
            CategorySongs categorySongs = new CategorySongs(getApplicationContext());
            categorySongs.open();
            if (categorySongs.checkRow(path)) {
                categorySongs.updateRow(path);
            } else {
                categorySongs.addRow(1, songsUtils.queue().get(songsUtils.getCurrentMusicID()));
            }
            categorySongs.close();
        } catch (Exception e) {
        }
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if (TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.app_name), null);
        }

        return null;
    }

    //Not important for general audio service, required for class
    @Override
    public void onLoadChildren(String parentId, Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    /******* ---------------------------------------------------------------
     Music Widgets
     ----------------------------------------------------------------*******/

    private void musicWidgetsReset() {
        updateMusicWidget(this, MusicWidget4x1.class);
        updateMusicWidget(this, MusicWidget4x1v2.class);
        updateMusicWidget(this, MusicWidget4x2.class);
    }

    private void updateMusicWidget(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra("state", mPlaybackStateBuilder.build().getState());
        int[] ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, cls));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}
