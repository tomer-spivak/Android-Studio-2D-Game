package tomer.spivak.androidstudio2dgame.modelAnimations;

import tomer.spivak.androidstudio2dgame.modelEnums.EnemyState;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.IDamageable;

public class BreatheFire implements EnemyAttackAnimation {

    private long elapsedTime;
    private boolean running;
    private Enemy enemy;
    private IDamageable target;

    // Constants for delays (in milliseconds)
    private final int initDelay = 500;
    private final int stepDelay = 150;
    private final int repeatCount = 10;

    // Total duration is calculated from:
    // - 0ms initial state (ATTACKING1)
    // - initDelay for ATTACKING2
    // - then a repeated cycle of two states (ATTACKING3 and ATTACKING4) for repeatCount times,
    // - plus a final stepDelay to switch back to IDLE.

    @Override
    public void execute(Enemy enemy, IDamageable target) {
        this.enemy = enemy;
        this.target = target;
        enemy.resetAttackTimer();
        elapsedTime = 0;
        running = true;
        // Set initial state
        enemy.setState(EnemyState.ATTACKING1);
    }

    @Override
    public void update(long deltaTime) {
        if (!running) {
            return;
        }
        elapsedTime += deltaTime;

        if (elapsedTime < initDelay) {
            enemy.setState(EnemyState.ATTACKING1);
        } else if (elapsedTime < initDelay + initDelay) {
            enemy.setState(EnemyState.ATTACKING2);
        } else if (elapsedTime < initDelay + initDelay + (repeatCount * 2 * stepDelay)) {
            // Determine which cycle we are in
            long t = elapsedTime - 2 * initDelay;
            long cycleTime = t % (2 * stepDelay);

            if (cycleTime < stepDelay) {
                enemy.setState(EnemyState.ATTACKING3);
            } else {
                enemy.setState(EnemyState.ATTACKING4);
                // Trigger damage on the transition between states.
                // Depending on your game logic, you may want to trigger this once per cycle.
                enemy.dealDamage(target);
            }
        } else {
            // Animation finished: set state to IDLE, and stop the animation
            enemy.setState(EnemyState.IDLE);
            running = false;
            enemy.resetAttackTimer();
        }
    }

    @Override
    public void cancelAnimation() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getRepeatCount() {
        return repeatCount;
    }
}
