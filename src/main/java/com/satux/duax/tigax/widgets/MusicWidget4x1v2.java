package com.satux.duax.tigax.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

import com.satux.duax.tigax.MainActivity;
import com.satux.duax.tigax.R;
import com.satux.duax.tigax.services.MusicService;
import com.satux.duax.tigax.utils.ImageUtils;
import com.satux.duax.tigax.utils.SharedPrefsUtils;

public class MusicWidget4x1v2 extends AppWidgetProvider {

    int playbackState = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        playbackState = intent.getIntExtra("state", 0);
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // TODO Auto-generated method stub
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int appWidgetId1 : appWidgetIds) {
            try {
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget4x1v2);

                Intent previousIntent = new Intent(context, MusicService.class);
                Intent playIntent = new Intent(context, MusicService.class);
                Intent nextIntent = new Intent(context, MusicService.class);
                Intent repeatIntent = new Intent(context, MusicService.class);

                previousIntent.setAction(MusicService.ACTION_TRACK_PREV);
                playIntent.setAction(MusicService.ACTION_PLAY_PAUSE);
                nextIntent.setAction(MusicService.ACTION_TRACK_NEXT);
                repeatIntent.setAction(MusicService.ACTION_REPEAT);

                PendingIntent ppreviousIntent = PendingIntent.getService(context, 0,
                        previousIntent, 0);

                PendingIntent pplayIntent = PendingIntent.getService(context, 0,
                        playIntent, 0);

                PendingIntent pnextIntent = PendingIntent.getService(context, 0,
                        nextIntent, 0);

                views.setOnClickPendingIntent(R.id.playImageView, pplayIntent);
                views.setOnClickPendingIntent(R.id.prevImageView, ppreviousIntent);
                views.setOnClickPendingIntent(R.id.nextImageView, pnextIntent);
                views.setOnClickPendingIntent(R.id.albumArtImageView, pendingIntent);
                views.setOnClickPendingIntent(R.id.titleTextView, pendingIntent);
                views.setOnClickPendingIntent(R.id.albumTextView, pendingIntent);

                if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
                    views.setImageViewResource(R.id.playImageView, R.drawable.app_pause);
                } else {
                    views.setImageViewResource(R.id.playImageView, R.drawable.app_play);
                }

                SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils(context);

                if (!sharedPrefsUtils.readSharedPrefsString("albumid", "0").equals("0")) {
                    views.setImageViewBitmap(
                            R.id.albumArtImageView,
                            (new ImageUtils(context))
                                    .getAlbumArt(
                                            Long.parseLong(sharedPrefsUtils.readSharedPrefsString("albumid", "0"))
                                    )
                    );

                    views.setTextViewText(R.id.titleTextView, sharedPrefsUtils.readSharedPrefsString("title", "title"));
                    views.setTextViewText(R.id.albumTextView, sharedPrefsUtils.readSharedPrefsString("album", "album"));
                }

                appWidgetManager.updateAppWidget(appWidgetId1, views);
            } catch (Exception e) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widgetblank);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
                views.setOnClickPendingIntent(R.id.titleTextView, pendingIntent);
                appWidgetManager.updateAppWidget(appWidgetId1, views);
            }
        }
    }
}
