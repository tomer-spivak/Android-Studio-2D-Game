package tomer.spivak.androidstudio2dgame.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.util.Random;

import tomer.spivak.androidstudio2dgame.R;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private final int[] music = {R.raw.candyland, R.raw.fade, R.raw.infectious,
            R.raw.invincible, R.raw.sky_high, R.raw.spectre};
    private final Random random = new Random();
    private int lastSongIndex = -1;
    float volume;
    public static final String CHANNEL_ID = "music_service_channel";

    private final IBinder binder = new LocalBinder();

    public int getCurrentVolumeLevel() {
        return (int) (volume * 100);
    }

    public float getVolume() {
        return volume;
    }

    public void setVolumeLevel(float progress) {
        if (mediaPlayer != null) {
            volume = progress / 100;
            mediaPlayer.setVolume(volume, volume);


        }
    }

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        volume = prefs.getFloat("volume", 0.07f) ;
        NotificationChannel chan = new NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager mgr = getSystemService(NotificationManager.class);
        mgr.createNotificationChannel(chan);
    }



    private void playRandomSong() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        int newSongIndex;
        do {
            newSongIndex = random.nextInt(music.length);
        } while (newSongIndex == lastSongIndex);

        lastSongIndex = newSongIndex;

        mediaPlayer = MediaPlayer.create(this, music[newSongIndex]);

        mediaPlayer.setVolume(volume, volume);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playRandomSong();
            }
        });
        mediaPlayer.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaPlayer == null) {
            playRandomSong();
        }
        Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Game Music")
                .setContentText("Playing background music")
                .setSmallIcon(R.drawable.logo)   // your app’s music icon
                .setOngoing(true)
                .build();

        // Enter foreground mode:
        startForeground(1, notif);
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }



    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resumeMusic() {
        if (mediaPlayer != null) {
            try {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    mediaPlayer.setVolume(volume, volume);
                }
            } catch (IllegalStateException ignored) {
            }
        }
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (IllegalStateException ignored) {
            } finally {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }

}
