package tomer.spivak.androidstudio2dgame.modelAnimations;

import tomer.spivak.androidstudio2dgame.modelEnums.EnemyState;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.IDamageable;

public class BreatheFire implements EnemyAttackAnimation {

    private long elapsedTime;
    private boolean running;
    private Enemy enemy;
    private IDamageable target;

    private final int repeatCount = 10;


    @Override
    public void execute(Enemy enemy, IDamageable target) {
        this.enemy = enemy;
        this.target = target;
        enemy.resetAttackTimer();
        elapsedTime = 0;
        running = true;
        enemy.setState(EnemyState.ATTACKING1);
    }

    @Override
    public void update(long deltaTime) {
        if (!running) {
            return;
        }
        elapsedTime += deltaTime;

        int initDelay = 500;
        int stepDelay = 150;
        if (elapsedTime < initDelay) {
            enemy.setState(EnemyState.ATTACKING1);
        } else if (elapsedTime < initDelay + initDelay) {
            enemy.setState(EnemyState.ATTACKING2);
        } else if (elapsedTime < initDelay + initDelay + (repeatCount * 2 * stepDelay)) {
            long t = elapsedTime - 2 * initDelay;
            long cycleTime = t % (2 * stepDelay);

            if (cycleTime < stepDelay) {
                enemy.setState(EnemyState.ATTACKING3);
            } else {
                enemy.setState(EnemyState.ATTACKING4);
                enemy.dealDamage(target);
            }
        } else {
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
