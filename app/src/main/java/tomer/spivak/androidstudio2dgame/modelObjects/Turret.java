package tomer.spivak.androidstudio2dgame.modelObjects;

import android.util.Log;
import tomer.spivak.androidstudio2dgame.model.AttackComponent;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.TurretState;
import tomer.spivak.androidstudio2dgame.modelEnums.TurretType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Turret extends Building implements IDamager{
    private final AttackComponent attackComponent;
    private final float attackRange;
    private final TurretType type;
    private TurretState state;

    public Turret(float health, float attackDamage, float attackRange, Position pos,
                  TurretType type, long attackCooldown) {
        super(health, pos);
        this.attackRange = attackRange;
        this.type = type;
        attackComponent = new AttackComponent(attackDamage, attackCooldown);
        this.state = TurretState.IDLE;
    }

    public Enemy findTarget(List<Enemy> enemies) {
        double minDistance = Double.MAX_VALUE;
        List<Enemy> closetEnemies = new ArrayList<>();

        for (Enemy enemy : enemies) {
            double distance = pos.distanceTo(enemy.getPosition());
            Log.d("debug", "Distance to enemy: " + distance);

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
        if (closestEnemy != null)
            Log.d("turret", "Closest enemy: " + pos.distanceTo(closestEnemy.getPosition()));
        return closestEnemy;
    }

    // This method should be called in the main game loop.
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

    private boolean canAttack() {
        return attackComponent.canAttack() && state != TurretState.ATTACKING &&
                state != TurretState.HURT;
    }

    public void attack(Enemy target) {
        // Use a Handler to post a delayed task to the main thread (if needed)
        // Use a Handler to post a delayed task to the main thread (if needed)
        dealDamage(target);
        setState(TurretState.ATTACKING);

        // Schedule a task to reset the state after 200ms
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // After 200ms, reset the state and the attack timer.
                setState(TurretState.IDLE);
                attackComponent.resetAttackTimer();
            }
        }, 200);
    }


    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);
        setState(TurretState.HURT);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setState(TurretState.IDLE);
            }
        }, 200);
    }

    @Override
    public void dealDamage(IDamageable target) {
        attackComponent.dealDamage(target);
    }


    public TurretType getType() {
        return type;
    }

    public void setState(TurretState turretState) {
        this.state = turretState;
    }

    @Override
    public Object toMap() {
        Map turretData = (Map) super.toMap();
        // Store the type of object
        turretData.put("type", type.name());

        turretData.put("attackDamage", attackComponent.getAttackDamage());
        turretData.put("attackRange", attackRange);
        return turretData;
    }


}
