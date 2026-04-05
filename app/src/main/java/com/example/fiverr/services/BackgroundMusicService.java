package com.example.fiverr.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.fiverr.R;

public class BackgroundMusicService extends Service {

    private static final String CHANNEL_ID = "fiverr_music";
    private static final int NOTIFICATION_ID = 1001;
    public static final String ACTION_PLAY = "com.example.fiverr.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.fiverr.ACTION_PAUSE";

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        mediaPlayer = MediaPlayer.create(this, R.raw.ambient);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0.5f, 0.5f);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_PAUSE:
                    pauseMusic();
                    break;
                case ACTION_PLAY:
                    playMusic();
                    break;
                default:
                    playMusic();
                    break;
            }
        } else {
            playMusic();
        }
        return START_STICKY;
    }

    private void playMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
        }
        startForeground(NOTIFICATION_ID, buildNotification(true));
    }

    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        }
        startForeground(NOTIFICATION_ID, buildNotification(false));
    }

    private Notification buildNotification(boolean playing) {
        Intent toggleIntent = new Intent(this, BackgroundMusicService.class);
        toggleIntent.setAction(playing ? ACTION_PAUSE : ACTION_PLAY);
        PendingIntent togglePending = PendingIntent.getService(this, 0, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String actionLabel = playing ? "Pause" : "Play";
        int actionIcon = playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.music_notification_title))
                .setContentText(playing ? getString(R.string.music_notification_text) : "Music paused")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .addAction(actionIcon, actionLabel, togglePending)
                .setOngoing(true)
                .setSilent(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.music_channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.music_channel_desc));
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false;
        super.onDestroy();
    }
}
