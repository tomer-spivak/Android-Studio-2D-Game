package tomer.spivak.androidstudio2dgame.modelObjects;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.TurretType;

public class TargetingTurret extends Turret{
    public TargetingTurret(float health, float attackDamage, float attackRange, Position pos,
                           TurretType type, long attackCooldown) {
        super(health, attackDamage, attackRange, pos, type, attackCooldown);
    }

    public Enemy findTarget(List<Enemy> enemies) {
        double minDistance = Double.MAX_VALUE;
        List<Enemy> closetEnemies = new ArrayList<>();

        for (Enemy enemy : enemies) {
            double distance = pos.distanceTo(enemy.getPosition());

            if (distance <= attackRange && distance == minDistance){
                closetEnemies.add(enemy);
            }
            if(distance <= attackRange && distance < minDistance) {
                minDistance = distance;
                closetEnemies.clear();
                closetEnemies.add(enemy);
            }
        }
        float lowestHealth = Float.MAX_VALUE;
        Enemy closestEnemy = null;
        for (Enemy enemy : closetEnemies) {
            if (enemy.getHealth() < lowestHealth){
                lowestHealth = enemy.getHealth();
                closestEnemy = enemy;
            }
        }
        return closestEnemy;
    }


    public void update(List<Enemy> enemies, long elapsedTime) {
        // Check if enough time has passed since the last attack.

        attackComponent.accumulateAttackTime(elapsedTime);
        Log.d("turret", String.valueOf(attackComponent.getAttackTime()));
        if (canAttack()) {
            Enemy target = findTarget(enemies);
            if (target != null) {
                attack(target);
                // Optionally, trigger an animation or create a projectile here.
            }
        }
    }

}
