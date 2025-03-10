package tomer.spivak.androidstudio2dgame.modelAnimations;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tomer.spivak.androidstudio2dgame.modelEnums.EnemyState;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.IDamageable;

public class BreatheFire implements EnemyAttackAnimation {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Runnable> animationRunnables = new ArrayList<>();

    @Override
    public void execute(Enemy enemy, IDamageable target) {
        enemy.resetAttackTimer();
        final EnemyState[] initialStates = { EnemyState.ATTACKING1, EnemyState.ATTACKING2 };
        final int repeatCount = 3;
        final List<EnemyState> stateSequence = new ArrayList<>();
        final List<Integer> delays = new ArrayList<>();
        delays.add(0);

        Collections.addAll(stateSequence, initialStates);
        int initDelay = 500;
        delays.add(initDelay);
        delays.add(initDelay);
        int stepDelay = 150;

        for (int i = 0; i < repeatCount; i++) {
            stateSequence.add(EnemyState.ATTACKING3);
            delays.add(stepDelay);
            stateSequence.add(EnemyState.ATTACKING4);
            delays.add(stepDelay);
        }

        stateSequence.add(EnemyState.IDLE);
        delays.add(stepDelay);

        int accumulatedDelay = 0;
        for (int i = 0; i < stateSequence.size(); i++) {
            final EnemyState state = stateSequence.get(i);
            accumulatedDelay += delays.get(i);
            Runnable runnable = () -> {
                enemy.setState(state);
                if (state == EnemyState.IDLE) {
                    enemy.dealDamage(target);
                    enemy.resetAttackTimer();
                }
            };
            animationRunnables.add(runnable);
            handler.postDelayed(runnable, accumulatedDelay);
        }
    }


    public void cancelAnimation() {
        for (Runnable runnable : animationRunnables) {
            handler.removeCallbacks(runnable);
        }
        animationRunnables.clear();
    }
}