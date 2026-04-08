package com.example.fiverr.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.fiverr.R;

public class BackgroundMusicService extends Service {

    private static final String TAG = "BackgroundMusicService";
    private static final String CHANNEL_ID = "fiverr_music";
    private static final int NOTIFICATION_ID = 1001;
    public static final String ACTION_PLAY = "com.example.fiverr.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.fiverr.ACTION_PAUSE";

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private boolean isPlaying = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        initMediaPlayer();
    }

    private void initMediaPlayer() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.ambient);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(0.5f, 0.5f);
                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "MediaPlayer error: what=" + what + " extra=" + extra);
                    isPlaying = false;
                    // Try to recover
                    mp.reset();
                    try {
                        mp.setDataSource(getApplicationContext(),
                                android.net.Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ambient));
                        mp.prepareAsync();
                    } catch (Exception e) {
                        Log.e(TAG, "Recovery failed", e);
                    }
                    return true;
                });
            } else {
                Log.e(TAG, "MediaPlayer.create() returned null — ambient.mp3 may be corrupt or missing");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to init MediaPlayer", e);
            mediaPlayer = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // CRITICAL: Always call startForeground() immediately in onStartCommand
        // Android 14+ will throw ForegroundServiceStartNotAllowedException if delayed
        startForeground(NOTIFICATION_ID, buildNotification(isPlaying));

        String action = (intent != null && intent.getAction() != null) ? intent.getAction() : ACTION_PLAY;

        switch (action) {
            case ACTION_PAUSE:
                pauseMusic();
                break;
            case ACTION_PLAY:
            default:
                playMusic();
                break;
        }

        // If mediaPlayer failed to init, stop service cleanly
        if (mediaPlayer == null) {
            Log.e(TAG, "MediaPlayer not available, stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }

        return START_STICKY;
    }

    private void playMusic() {
        if (mediaPlayer == null) return;

        // Request audio focus
        boolean focusGranted = requestAudioFocus();
        if (!focusGranted) {
            Log.w(TAG, "Audio focus not granted");
            return;
        }

        try {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                isPlaying = true;
            }
            // Update notification to reflect playing state
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.notify(NOTIFICATION_ID, buildNotification(true));
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "MediaPlayer in bad state during play", e);
            // Re-init and try again
            mediaPlayer.release();
            initMediaPlayer();
            if (mediaPlayer != null) {
                mediaPlayer.start();
                isPlaying = true;
            }
        }
    }

    private void pauseMusic() {
        if (mediaPlayer == null) return;
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPlaying = false;
            }
            // Update notification
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.notify(NOTIFICATION_ID, buildNotification(false));
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "MediaPlayer in bad state during pause", e);
        }
        abandonAudioFocus();
    }

    private boolean requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(attrs)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(focusChange -> {
                        switch (focusChange) {
                            case AudioManager.AUDIOFOCUS_LOSS:
                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                                pauseMusic();
                                break;
                            case AudioManager.AUDIOFOCUS_GAIN:
                                playMusic();
                                break;
                        }
                    })
                    .build();
            int result = audioManager.requestAudioFocus(audioFocusRequest);
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                    || result == AudioManager.AUDIOFOCUS_REQUEST_DELAYED;
        } else {
            int result = audioManager.requestAudioFocus(
                    focusChange -> {
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) pauseMusic();
                        else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) playMusic();
                    },
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
            );
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
    }

    private void abandonAudioFocus() {
        if (audioManager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        } else {
            audioManager.abandonAudioFocus(null);
        }
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
                .setOngoing(playing)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.music_channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.music_channel_desc));
            channel.setSound(null, null); // Silent channel
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
        abandonAudioFocus();
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing MediaPlayer", e);
            }
            mediaPlayer = null;
        }
        isPlaying = false;
        super.onDestroy();
    }
}
