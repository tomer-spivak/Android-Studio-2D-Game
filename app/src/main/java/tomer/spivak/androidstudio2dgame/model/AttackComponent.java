package tomer.spivak.androidstudio2dgame.model;

import tomer.spivak.androidstudio2dgame.modelObjects.IDamageable;

public class AttackComponent {
    private float attackDamage;
    private final float attackCooldown;
    private float timeSinceLastAttack = 0;

    public AttackComponent(float attackDamage, float attackCooldown) {
        this.attackDamage = attackDamage;
        this.attackCooldown = attackCooldown;
    }

    public void accumulateAttackTime(long elapsedTime) {
        timeSinceLastAttack += elapsedTime;
    }

    public boolean canAttack() {
        return timeSinceLastAttack >= attackCooldown;
    }

    public void resetAttackTimer() {
        timeSinceLastAttack = 0;
    }

    public void dealDamage(IDamageable target) {
        target.takeDamage(attackDamage);
    }

    public void setTimeSinceLastAttack(float timeSinceLastAttack) {
        this.timeSinceLastAttack = timeSinceLastAttack;
    }

    public void setAttackDamage(float attackDamage) {
        this.attackDamage = attackDamage;
    }

    public float getAttackDamage() {
        return attackDamage;
    }

    public float getTimeSinceLastAttack() {
        return timeSinceLastAttack;
    }
}

