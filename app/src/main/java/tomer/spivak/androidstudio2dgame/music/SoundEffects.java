package tomer.spivak.androidstudio2dgame.music;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import tomer.spivak.androidstudio2dgame.R;

public class SoundEffects {
        private SoundPool soundPool;
        private final int enemyAttackSound;
        private final int turretAttackSound;

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
            return soundPool.play(enemyAttackSound, 1.0f, 1.0f, 1,
                    0, 1.0f);
        }
        public int playTurretAttackSound() {
            return soundPool.play(turretAttackSound, 1.0f, 1.0f, 1,
                    0, 1.0f);
        }
    public void stopSound(int streamId) {
        soundPool.stop(streamId);
    }


    public void onDestroy() {
            soundPool.release();
            soundPool = null;
        }


}
