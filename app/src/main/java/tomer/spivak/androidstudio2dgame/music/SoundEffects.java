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
            enemyAttackSound = soundPool.load(context, R.raw.monsterattack, 1);
            turretAttackSound = soundPool.load(context, R.raw.lightningtowerattack, 1);
        }

        // Play sound when needed
        public int playEnemyAttackSound() {
            int streamId = soundPool.play(enemyAttackSound, 1.0f, 1.0f, 1,
                    0, 1.0f);
            soundIds.add(streamId);

            return streamId;
        }
        public int playTurretAttackSound() {
            int streamId = soundPool.play(turretAttackSound, 1.0f, 1.0f, 1,
                    0, 1.0f);
            soundIds.add(streamId);
            return streamId;
        }
    public void stopSound(int streamId) {
        soundPool.stop(streamId);
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
}
