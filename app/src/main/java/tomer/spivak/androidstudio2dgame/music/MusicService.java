package tomer.spivak.androidstudio2dgame.music;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.util.Random;

import tomer.spivak.androidstudio2dgame.R;

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
        Log.d("music", "MusicService onBind called");
        return binder;
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
    public void stopMusic() {
        if (mediaPlayer != null) {
            try {
                // Check if MediaPlayer is playing or in a valid state
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (IllegalStateException e) {
                // Optionally log or handle the exception
                Log.e("music", "MediaPlayer stop failed: " + e.getMessage());
            } finally {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }

}
