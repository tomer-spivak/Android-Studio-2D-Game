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
                    .setMaxStreams(5) // Up to 5 sounds at once
                    .setAudioAttributes(audioAttributes)
                    .build();

            // Load sound
            enemyAttackSound = soundPool.load(context, R.raw.enemyattack, 1);
            turretAttackSound = soundPool.load(context, R.raw.turretattack, 1);
        }

        // Play sound when needed
        public void playEnemyAttackSound() {
            soundPool.play(enemyAttackSound, 1.0f, 1.0f, 1, 0, 1.0f);
        }
        public void playTurretAttackSound() {
            soundPool.play(turretAttackSound, 1.0f, 1.0f, 1, 0, 1.0f);
        }

        public void onDestroy() {
            soundPool.release();
            soundPool = null;
        }


}
