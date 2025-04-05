package tomer.spivak.androidstudio2dgame.music;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import java.util.ArrayList;
import java.util.List;

import tomer.spivak.androidstudio2dgame.R;

public class SoundEffects {
    private SoundPool soundPool;
    private final int enemyAttackSound;
    private final int turretAttackSound;
    private final List<Integer> soundIds = new ArrayList<>();
    private float volume;


    public SoundEffects(Context context) {
        // Initialize SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(50) // Up to 5 sounds at once
                    .setAudioAttributes(audioAttributes)
                    .build();

            // Load sound
        volume = 0.5f;
            enemyAttackSound = soundPool.load(context, R.raw.monsterattack, 1);
            turretAttackSound = soundPool.load(context, R.raw.lightningtowerattack, 1);
        }

    public int playEnemyAttackSound() {
            int streamId = soundPool.play(enemyAttackSound, volume, volume, 1,
                    0, 1.0f);
            soundIds.add(streamId);

            return streamId;
    }

    public int playTurretAttackSound() {
            int streamId = soundPool.play(turretAttackSound, volume, volume, 1,
                    0, 1.0f);
            soundIds.add(streamId);
            return streamId;
    }

    public void stopSound(int streamId) {
        soundPool.stop(streamId);
    }

    public float getVolume() {
       return volume;
    }

    public int getVolumeLevel(){
        return (int) (getVolume() * 100);
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void onDestroy() {
            soundPool.release();
            soundPool = null;
        }

    public void pauseSoundEffects() {
            soundPool.autoPause();
    }

    public void resumeSoundEffects(){
            soundPool.autoResume();
    }

    public void stopSoundEffects() {

        for (int soundId : soundIds) {
            soundPool.stop(soundId);
        }
    }

    public void pauseSoundEffect(int soundStreamId) {
        soundPool.pause(soundStreamId);

    }

    public void resumeSoundEffect(int soundStreamId) {
        soundPool.resume(soundStreamId);
    }
}
