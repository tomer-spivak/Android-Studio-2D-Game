package tomer.spivak.androidstudio2dgame.projectManagement;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import java.util.ArrayList;

import tomer.spivak.androidstudio2dgame.R;

public class SoundEffectManager {
    private SoundPool soundPool;
    private final int enemyAttackSound;
    private final int turretAttackSound;
    private final ArrayList<Integer> soundIds = new ArrayList<>();
    private float volume;

    public SoundEffectManager(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        soundPool = new SoundPool.Builder().setMaxStreams(50).setAudioAttributes(audioAttributes).build();
        volume = 0.5f;
        enemyAttackSound = soundPool.load(context, R.raw.monsterattack, 1);
        turretAttackSound = soundPool.load(context, R.raw.lightningtowerattack, 1);
    }

    public int playEnemyAttackSound() {
        if (soundPool == null)
            return 0;
        int streamId = soundPool.play(enemyAttackSound, volume, volume, 1, 0, 1.0f);
        soundIds.add(streamId);
        return streamId;
    }

    public int playTurretAttackSound() {
        int streamId = soundPool.play(turretAttackSound, volume, volume, 1, 0, 1.0f);
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
        this.volume = Math.max(0f, Math.min(1f, volume));
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

    public void stopAllSoundEffects() {
        for (int soundId : soundIds) {
            soundPool.stop(soundId);
        }
        soundIds.clear();
    }

    public void pauseSoundEffect(int soundStreamId) {
        soundPool.pause(soundStreamId);
    }

    public void resumeSoundEffect(int soundStreamId) {
        soundPool.resume(soundStreamId);
    }
}
