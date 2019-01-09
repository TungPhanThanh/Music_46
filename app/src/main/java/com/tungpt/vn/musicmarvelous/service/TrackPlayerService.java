package com.example.themo.musicmarvelous.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.themo.musicmarvelous.R;
import com.example.themo.musicmarvelous.constants.Constants;
import com.example.themo.musicmarvelous.constants.LoopMode;
import com.example.themo.musicmarvelous.constants.ShuffleMode;
import com.example.themo.musicmarvelous.constants.State;
import com.example.themo.musicmarvelous.data.model.Track;
import com.example.themo.musicmarvelous.ui.main.play.PlayTrackActivity;

import java.util.List;

public class TrackPlayerService extends Service {

    private static final String TITLE_PLAY = "Play";
    private static final String TITLE_PAUSE = "Pause";
    private static final String TITLE_NEXT = "Next";
    private static final String TITLE_PREVIOUS = "Previous";

    public static final String ACTION_CHANGE_STATE = "ACTION_CHANGE_STATE";
    public static final String ACTION_NEXT_TRACK = "ACTION_NEXT_TRACK";
    public static final String ACTION_PREVIOUS_TRACK = "ACTION_PREVIOUS_TRACK";
    public static final String ACTION_OPEN_PLAY_TRACK_ACTIVITY = "ACTION_OPEN_PLAY_TRACK_ACTIVITY";
    public static final int SECONDS_FACTOR = 1000;
    private static final int NOTIFY_ID = 1;

    private Bitmap mBitmap;
    private PendingIntent mPendingOpenApp;
    private PendingIntent mPendingPrevious;
    private PendingIntent mPendingState;
    private PendingIntent mPendingNext;
    private NotificationCompat.Builder mBuilder;
    private ManagerTrackPlayer mTrackPlayerManager;
    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        handleIntent(intent);
        return mBinder;
    }

    public void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        switch (intent.getAction()) {
            case ACTION_CHANGE_STATE:
                if (getMediaState() != State.PREPARE) {
                    changeTrackState();
                }
                break;
            case ACTION_PREVIOUS_TRACK:
                playPrevious();
                break;
            case ACTION_NEXT_TRACK:
                playNext();
                break;
            default:
                break;
        }
    }

    public Track getCurrentTrack() {
        return mTrackPlayerManager != null ? mTrackPlayerManager.getCurrentTrack() : null;
    }

    public void setTrackInfoListener(TrackPlayerController.TrackInfoListener listener) {
        if (mTrackPlayerManager == null) return;
        mTrackPlayerManager.setTrackInfoListener(listener);
    }

    public void actionSeekTo(int userSelectedPosition) {
        if (mTrackPlayerManager != null) {
            mTrackPlayerManager.seekTo(userSelectedPosition);
        }
    }

    public void changeTrackState() {
        if (mTrackPlayerManager != null) {
            mTrackPlayerManager.changeTrackState();
        }
    }

    public void playNext() {
        if (mTrackPlayerManager != null) {
            mTrackPlayerManager.playNextTrack();
        }
    }

    public void playPrevious() {
        if (mTrackPlayerManager != null) {
            mTrackPlayerManager.playPreviousTrack();
        }
    }

    public int getMediaState() {
        if (mTrackPlayerManager == null) return State.INVALID;
        return mTrackPlayerManager.getCurrentState();
    }

    public List<Track> getListTrack() {
        if (mTrackPlayerManager == null) return null;
        return mTrackPlayerManager.getListTracks();
    }

    public void playTrackAtPosition(int position, Track... tracks) {
        if (mTrackPlayerManager == null) {
            mTrackPlayerManager = new TrackPlayerController(this);
        }
        mTrackPlayerManager.playTrackAtPosition(position, tracks);
    }

    public void addToNextUp(Track track) {
        if (mTrackPlayerManager != null) {
            mTrackPlayerManager.addToNextUp(track);
        }
    }

    public int getCurrentTrackPosition() {
        if (mTrackPlayerManager == null) return 0;
        return mTrackPlayerManager.getCurrentTrackPosition();
    }

    public void changeLoopType() {
        if (mTrackPlayerManager != null) {
            mTrackPlayerManager.changeLoopType();
        }
    }

    @LoopMode
    public int getLoopMode() {
        if (mTrackPlayerManager == null) return LoopMode.NO_LOOP;
        return mTrackPlayerManager.getLoopMode();
    }

    public void changeShuffleState() {
        if (mTrackPlayerManager != null) {
            mTrackPlayerManager.changeShuffleState();
        }
    }

    public int getShuffleMode() {
        if (mTrackPlayerManager != null) {
            return mTrackPlayerManager.getShuffleMode();
        }
        return ShuffleMode.OFF;
    }

    public void createNotification(@State int state) {
        if (getCurrentTrack() == null) return;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        setupBaseNotification();
        setupNotificationChannel(notificationManager);
        if (state == State.PAUSE) {
            stopForeground(false);
            mBuilder.setOngoing(false)
                    .addAction(R.drawable.ic_noti_previous, TITLE_PREVIOUS, mPendingPrevious)
                    .addAction(R.drawable.ic_play_arrow, TITLE_PLAY, mPendingState)
                    .addAction(R.drawable.ic_noti_next, TITLE_NEXT, mPendingNext)
                    .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2));
            notificationManager.notify(NOTIFY_ID, mBuilder.build());
        } else {
            mBuilder.addAction(R.drawable.ic_noti_previous, TITLE_PREVIOUS, mPendingPrevious)
                    .addAction(R.drawable.ic_pause, TITLE_PAUSE, mPendingState)
                    .addAction(R.drawable.ic_noti_next, TITLE_NEXT, mPendingNext)
                    .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2));
            startForeground(NOTIFY_ID, mBuilder.build());
        }
    }

    private void setupNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelID = getString(R.string.channel_id);
            String channelName = getString(R.string.channel_name);
            String channelDescription = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notificationManager.getNotificationChannel(channelID);
            if (mChannel == null) {
                mChannel = new NotificationChannel(channelID, channelName, importance);
                mChannel.setDescription(channelDescription);
                mChannel.enableVibration(false);
                mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationManager.createNotificationChannel(mChannel);
            }
        }
    }

    public void loadImage() {
        if (getCurrentTrack() == null) return;
        Glide.with(this)
                .asBitmap()
                .load(getCurrentTrack().getArtworkUrl())
                .apply(new RequestOptions().error(R.drawable.logo_app))
                .into(new SimpleTarget<Bitmap>(Constants.DEFAULT_NOTIFY_SIZE, Constants.DEFAULT_NOTIFY_SIZE) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource,
                                                @Nullable Transition<? super Bitmap> transition) {
                        mBitmap = resource;
                        mBuilder.setLargeIcon(mBitmap);
                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(NOTIFY_ID, mBuilder.build());
                    }
                });
    }

    private void setupBaseNotification() {

        Intent notificationIntent = new Intent(getApplicationContext(), PlayTrackActivity.class);
        notificationIntent.setAction(ACTION_OPEN_PLAY_TRACK_ACTIVITY);
        mPendingOpenApp = PendingIntent.getActivity(getApplicationContext(),
                0, notificationIntent, 0);

        Intent nextIntent = new Intent(getApplicationContext(), TrackPlayerService.class);
        nextIntent.setAction(ACTION_NEXT_TRACK);
        mPendingNext = PendingIntent.getService(getApplicationContext(), 0,
                nextIntent, 0);

        Intent prevIntent = new Intent(getApplicationContext(), TrackPlayerService.class);
        prevIntent.setAction(ACTION_PREVIOUS_TRACK);
        mPendingPrevious = PendingIntent.getService(getApplicationContext(), 0,
                prevIntent, 0);

        Intent stateIntent = new Intent(getApplicationContext(), TrackPlayerService.class);
        stateIntent.setAction(ACTION_CHANGE_STATE);
        mPendingState = PendingIntent.getService(getApplicationContext(), 0,
                stateIntent, 0);
        mBuilder = new NotificationCompat.Builder(getApplicationContext(),
                getString(R.string.channel_id))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(getCurrentTrack().getTitle())
                .setContentText(getCurrentTrack().getPublisherAlbumTitle())
                .setColor(getResources().getColor(R.color.color_gray_600))
                .setSmallIcon(R.drawable.logo_app)
                .setContentIntent(mPendingOpenApp);
        if (mBitmap == null) {
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_disc));
        } else {
            mBuilder.setLargeIcon(mBitmap);
        }
    }

    public int getCurrentPosition() {
        if (mTrackPlayerManager != null) {
            return mTrackPlayerManager.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (mTrackPlayerManager != null) {
            return mTrackPlayerManager.getDuration();
        }
        return 0;
    }

    public class LocalBinder extends Binder {
        public TrackPlayerService getService() {
            return TrackPlayerService.this;
        }
    }
}
