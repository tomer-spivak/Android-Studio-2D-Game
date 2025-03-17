package tomer.spivak.androidstudio2dgame;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.util.Random;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private final int[] music = {R.raw.candyland, R.raw.fade, R.raw.infectious,
            R.raw.invincible, R.raw.sky_high, R.raw.spectre};
    private final Random random = new Random();
    private int lastSongIndex = -1;
    final float Volume = 0.2f;

    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        playRandomSong(); // Start with a random song
        Log.d("music", "MusicService onCreate");
    }

    private void playRandomSong() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        int newSongIndex;
        do {
            newSongIndex = random.nextInt(music.length); // Pick a random index
        } while (newSongIndex == lastSongIndex);
        lastSongIndex = newSongIndex;
        mediaPlayer = MediaPlayer.create(this, music[newSongIndex]);
        mediaPlayer.start();
        mediaPlayer.setVolume(Volume, Volume);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playRandomSong();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, createNotification()); // Start as a foreground service
        Log.d("music", "MusicService onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private Notification createNotification() {
        String channelId = "music_service_channel";
        NotificationChannel channel = new NotificationChannel(channelId, "Music Service",
                NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Music Playing")
                .setContentText("Your music is playing in the background.")
                .setSmallIcon(R.drawable.logo) // Your icon in res/drawable
                .build();
    }

    public void pauseMusic() {
        Log.d("music", mediaPlayer != null ? "MediaPlayer exists" : "MediaPlayer is null");
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resumeMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }
}
