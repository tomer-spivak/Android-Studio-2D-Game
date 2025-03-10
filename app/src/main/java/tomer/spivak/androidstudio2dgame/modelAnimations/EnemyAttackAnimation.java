package tomer.spivak.androidstudio2dgame.modelAnimations;

import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.IDamageable;

public interface EnemyAttackAnimation {
    void execute(Enemy enemy, IDamageable target);

    void cancelAnimation();
}