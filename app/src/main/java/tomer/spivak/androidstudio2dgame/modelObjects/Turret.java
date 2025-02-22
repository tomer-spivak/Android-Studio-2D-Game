package tomer.spivak.androidstudio2dgame.modelObjects;


import tomer.spivak.androidstudio2dgame.model.AttackComponent;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.TurretState;
import tomer.spivak.androidstudio2dgame.modelEnums.TurretType;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public abstract class Turret extends Building implements IDamager{
    protected final AttackComponent attackComponent;
    protected final float attackRange;
    protected final TurretType type;
    protected TurretState state;

    public Turret(float health, float attackDamage, float attackRange, Position pos,
                  TurretType type, long attackCooldown) {
        super(health, pos);
        this.attackRange = attackRange;
        this.type = type;
        attackComponent = new AttackComponent(attackDamage, attackCooldown);
        this.state = TurretState.IDLE;
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

    // This method should be called in the main game loop.
    protected boolean canAttack() {
        return attackComponent.canAttack() && state != TurretState.ATTACKING &&
                state != TurretState.HURT;
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
