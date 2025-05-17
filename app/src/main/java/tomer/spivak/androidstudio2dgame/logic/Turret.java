package tomer.spivak.androidstudio2dgame.logic;


import java.util.*;

import tomer.spivak.androidstudio2dgame.logic.modelEnums.BuildingState;

public class Turret extends Building {
    private final float attackDamage;
    private final float attackCooldown;
    private float timeSinceLastAttack = 0;
    private final ArrayList<Position> positionsToAttack = new ArrayList<>();
    private final ArrayList<Position> removedPositions = new ArrayList<>();
    private Enemy target;
    private long chargeTime = 0;
    private boolean isCharging = false;

    public Turret(float health, float attackDamage, Position pos, long attackCooldown) {
        super(health, pos, "lightningtower");
        this.attackDamage = attackDamage;
        this.attackCooldown = attackCooldown;
        for (int i = 0; i < 3; i++) {
            positionsToAttack.add(new Position(pos.getX() + i + 1, pos.getY()));
            positionsToAttack.add(new Position(pos.getX() - i - 1, pos.getY()));
            positionsToAttack.add(new Position(pos.getX(), pos.getY() + i + 1));
            positionsToAttack.add(new Position(pos.getX(), pos.getY() - i - 1));
        }
    }

    public void update(GameState state, long elapsedTime) {
        timeSinceLastAttack += elapsedTime;
        List<Position> stillAttackable = new ArrayList<>();
        for (Position positionToAttack : positionsToAttack) {
            if (shouldAttackPosition(positionToAttack, state)) {
                stillAttackable.add(positionToAttack);
            } else if (state.isValidPosition(positionToAttack)) {
                removedPositions.add(positionToAttack);
            }
        }
        positionsToAttack.clear();
        positionsToAttack.addAll(stillAttackable);

        List<Position> stillRemoved = new ArrayList<>();
        for (Position removedPosition : removedPositions) {
            if (shouldAttackPosition(removedPosition, state)) {
                positionsToAttack.add(removedPosition);
            } else {
                stillRemoved.add(removedPosition);
            }
        }
        removedPositions.clear();
        removedPositions.addAll(stillRemoved);

        if(isCharging){
            chargeTime += elapsedTime;
        }

        if (chargeTime > 200){
            setState(BuildingState.IDLE);
            target.takeDamage(attackDamage);
            timeSinceLastAttack = 0;
            chargeTime = 0;
            isCharging = false;
        }
    }

    public boolean executeAttack(List<Enemy> enemies) {
        boolean bool = false;
        if (timeSinceLastAttack >= attackCooldown && state != BuildingState.ATTACKING && state != BuildingState.HURT){
            for (Enemy enemy : enemies){
                for (Position pos : positionsToAttack){
                    if (pos.equals(enemy.getPosition())){
                        setSoundStreamId(soundEffects.playTurretAttackSound());
                        setState(BuildingState.ATTACKING);
                        this.target = enemy;
                        timeSinceLastAttack = 0;
                        bool = true;
                        isCharging = true;
                    }
                }
            }
        }
        return bool;
    }

    public ArrayList<Position> getCellsToAttack() {
        return positionsToAttack;
    }

    private boolean shouldAttackPosition(Position position, GameState current) {
        return current.isValidPosition(position) && !(current.getCellAt(position).getObject() instanceof Building);
    }

    @Override
    public Object toMap() {
        Map turretData = (Map) super.toMap();
        turretData.replace("type", "lightningtower");
        turretData.put("timeSinceLastAttack", timeSinceLastAttack);
        return turretData;
    }
}