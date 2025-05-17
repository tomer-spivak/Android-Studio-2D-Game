package tomer.spivak.androidstudio2dgame.modelObjects;


import java.util.*;

import tomer.spivak.androidstudio2dgame.logic.Building;
import tomer.spivak.androidstudio2dgame.logic.GameState;
import tomer.spivak.androidstudio2dgame.logic.Position;
import tomer.spivak.androidstudio2dgame.logic.modelEnums.BuildingState;

public class Turret extends Building implements IDamager {
    private final float attackDamage;
    private final float attackCooldown;
    private float timeSinceLastAttack = 0;
    protected final float attackRange;
    private final ArrayList<Position> positionsToAttack = new ArrayList<>();
    private final ArrayList<Position> removedPositions = new ArrayList<>();

    public Turret(float health, float attackDamage, float attackRange, Position pos, long attackCooldown) {
        super(health, pos, "lightningtower");
        this.attackRange = attackRange;
        this.attackDamage = attackDamage;
        this.attackCooldown = attackCooldown;
        this.type = "lightningtower";

        initCellsToAttack();

    }

    private void initCellsToAttack() {
        for (int i = 0; i < attackRange; i++) {
            positionsToAttack.add(new Position(pos.getX() + i + 1, pos.getY()));
            positionsToAttack.add(new Position(pos.getX() - i - 1, pos.getY()));
            positionsToAttack.add(new Position(pos.getX(), pos.getY() + i + 1));
            positionsToAttack.add(new Position(pos.getX(), pos.getY() - i - 1));
        }
    }

    public void update(GameState state, long elapsedTime) {
        accumulateAttackTime(elapsedTime);
        updateCellsToAttack(state);
    }

    public void updateCellsToAttack(GameState current) {
        Iterator<Position> iterator = positionsToAttack.iterator();
        while (iterator.hasNext()) {
            Position pos = iterator.next();
            if (!shouldAttackPosition(pos, current)) {
                if (current.isValidPosition(pos)) {
                    removedPositions.add(pos);
                }
                iterator.remove();
            }
        }

        Iterator<Position> removedIterator = removedPositions.iterator();
        while (removedIterator.hasNext()) {
            Position pos = removedIterator.next();
            if (shouldAttackPosition(pos, current)) {
                positionsToAttack.add(pos);
                removedIterator.remove();
            }
        }
    }

    public void accumulateAttackTime(long elapsedTime) {
        timeSinceLastAttack += elapsedTime;
    }

    public boolean executeAttack(List<Enemy> enemies) {
        boolean bool = false;
        if (!canAttack()){
            return false;
        }


        for (Enemy enemy : enemies){
            for (Position pos : positionsToAttack){
                if (pos.equals(enemy.getPosition())){
                    executeAttackSoundAndAnimation(enemy);
                    resetAttackTimer();
                    bool = true;
                }
            }
        }
        return bool;
    }

    public ArrayList<Position> getCellsToAttack() {
        return positionsToAttack;
    }

    private boolean shouldAttackPosition(Position position, GameState current) {
        return current.isValidPosition(position) &&
                !(current.getCellAt(position).getObject() instanceof Building);
    }

    public void executeAttackSoundAndAnimation(Enemy target) {
        setSoundStreamId(soundEffects.playTurretAttackSound());
        executeLightningAttackAnimation(target);
    }

    private void executeLightningAttackAnimation(Enemy target) {
        setState(BuildingState.ATTACKING);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setState(BuildingState.IDLE);
                dealDamage(target);
                resetAttackTimer();
            }
        }, 200);
    }

    protected boolean canAttack() {
        return timeSinceLastAttack >= attackCooldown && state != BuildingState.ATTACKING &&
                state != BuildingState.HURT;
    }

    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);
        setState(BuildingState.HURT);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                setState(BuildingState.IDLE);
            }
        }, 200);
    }

    @Override
    public void dealDamage(IDamageable target) {
        if (target == null) {
            return;
        }
        target.takeDamage(attackDamage);    }

    @Override
    public Object toMap() {
        Map<String,Object> turretData = (Map<String,Object>) super.toMap();
        turretData.replace("type", "lightningtower");
        turretData.put("timeSinceLastAttack", timeSinceLastAttack);
        turretData.put("attackRange", attackRange);
        return turretData;
    }

    public void resetAttackTimer() {
        timeSinceLastAttack = 0;
    }
}
