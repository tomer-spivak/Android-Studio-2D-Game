package tomer.spivak.androidstudio2dgame.modelObjects;


import tomer.spivak.androidstudio2dgame.model.AttackComponent;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelAnimations.TurretAnimationManager;
import tomer.spivak.androidstudio2dgame.modelEnums.AttackType;
import tomer.spivak.androidstudio2dgame.modelEnums.BuildingState;
import tomer.spivak.androidstudio2dgame.modelEnums.TurretType;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public abstract class Turret extends Building implements IDamager{
    protected final AttackComponent attackComponent;
    protected final float attackRange;
    protected final TurretType type;
    protected AttackType attackType;

    public Turret(float health, float attackDamage, float attackRange, Position pos,
                  TurretType type, long attackCooldown, AttackType attackType, int price) {
        super(health, pos, price);
        this.attackRange = attackRange;
        this.type = type;
        attackComponent = new AttackComponent(attackDamage, attackCooldown);
        this.attackType = attackType;
    }

    public void executeAttackSoundAndAnimation(Enemy target) {
        setSoundStreamId(soundEffects.playTurretAttackSound());
        TurretAnimationManager.executeAttackAnimation(this, target);
    }

    public void update(long deltaTime){

    }

    protected boolean canAttack() {
        return attackComponent.canAttack() && state != BuildingState.ATTACKING &&
                state != BuildingState.HURT;
    }

    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);
        setState(BuildingState.HURT);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setState(BuildingState.IDLE);
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


    public AttackType getAttackType() {
        return attackType;
    }


    @Override
    public Object toMap() {
        Map turretData = (Map) super.toMap();
        turretData.put("type", type.name());
        turretData.put("attackType", attackType.name());
        turretData.put("timeSinceLastAttack", attackComponent.getTimeSinceLastAttack());

        turretData.put("attackDamage", attackComponent.getAttackDamage());
        turretData.put("attackRange", attackRange);
        return turretData;
    }

    public void resetAttackTimer() {
        attackComponent.resetAttackTimer();
    }
}
