package tomer.spivak.androidstudio2dgame.logic;

import android.util.Log;

import java.util.List;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.logic.modelEnums.Direction;
import tomer.spivak.androidstudio2dgame.logic.modelEnums.EnemyState;

public class Enemy extends ModelObject {
    private final float movementSpeed;
    private Direction currentDirection;
    private EnemyState state;
    private List<Position> path;
    private int currentTargetIndex = 0;
    private float timeSinceLastMove = 0;
    private final int reward;
    private long attackAnimationElapsedTime;
    private boolean attackAnimationRunning = false;
    private Cell targetCell;
    private final float attackDamage;
    private final long attackCooldown;
    private long timeSinceLastAttack = 0;
    private long timeSinceTookDamage = 0;
    private boolean inTookDamageAnimation = false;
    private EnemyState stateBeforeHurt = EnemyState.IDLE;

    public Enemy(float health, float damage, float movementSpeed, Position pos, long attackCooldown, int reward) {
        super(health, pos);
        this.reward = reward;
        this.attackCooldown = attackCooldown;
        this.movementSpeed = movementSpeed;
        this.currentDirection = Direction.UPLEFT;
        this.state = EnemyState.IDLE;
        this.type = "monster";
        this.attackDamage = damage/10;
    }

    public void accumulateAttackTime(long deltaTime) {
        if(state == EnemyState.IDLE || state == EnemyState.HURT || !attackAnimationRunning)
            timeSinceLastAttack += deltaTime;
    }

    public void updateDirection(Position prevPos, Position nextPos) {
        if (prevPos.getY() > nextPos.getY()){
            currentDirection = Direction.UPRIGHT;
        } else if(prevPos.getY() < nextPos.getY()){
            currentDirection = Direction.DOWNLEFT;
        } else {
            if (nextPos.getX() < prevPos.getX()){
                currentDirection = Direction.UPLEFT;
            } else
                currentDirection = Direction.DOWNRIGHT;
        }
    }

    public void attemptAttack(Cell targetCell) {
        if (timeSinceLastAttack >= attackCooldown && (state == EnemyState.IDLE)) {
            setSoundStreamId(soundEffects.playEnemyAttackSound());
            this.targetCell = targetCell;
            state = EnemyState.ATTACKING1;
            attackAnimationElapsedTime = 0;
            attackAnimationRunning = true;
        }
    }

    public void update(long deltaTime){
        if (attackAnimationRunning) {
            attackAnimationElapsedTime += deltaTime;
            int initDelay = 500;
            int stepDelay = 150;
            if (attackAnimationElapsedTime < initDelay) {
                state = EnemyState.ATTACKING1;
            } else if (attackAnimationElapsedTime < initDelay + initDelay) {
                state = EnemyState.ATTACKING2;
            } else if (attackAnimationElapsedTime < initDelay + initDelay + (20 * stepDelay)) {
                if ((attackAnimationElapsedTime - 2 * initDelay) % (2 * stepDelay) < stepDelay) {
                    state = EnemyState.ATTACKING3;
                } else {
                    state = EnemyState.ATTACKING4;
                    if (targetCell.getObject() != null) {
                        targetCell.getObject().takeDamage(attackDamage);
                    }
                }
            } else {
                state = EnemyState.IDLE;
                attackAnimationRunning = false;
                timeSinceLastAttack = 0;
            }
        }
        if(inTookDamageAnimation){
            timeSinceTookDamage += deltaTime;
            if(timeSinceTookDamage > 700){
                setState(stateBeforeHurt);
                inTookDamageAnimation = false;
                soundEffects.resumeSoundEffect(soundStreamId);
                timeSinceTookDamage = 0;
            }
        }
    }

    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);
        if(health > 0){
            inTookDamageAnimation = true;
            if (getEnemyState() != EnemyState.HURT)
                stateBeforeHurt = getEnemyState();
            state = EnemyState.HURT;
            attackAnimationRunning = false;
            soundEffects.pauseSoundEffect(soundStreamId);
        }
    }

    @Override
    public Object toMap() {
        Map enemyData = (Map) super.toMap();
        enemyData.put("type", "monster");
        enemyData.put("currentDirection", currentDirection.name());
        enemyData.put("state",   state.name());
        enemyData.put("currentTargetIndex", currentTargetIndex);
        enemyData.put("timeSinceLastAttack", timeSinceLastAttack);
        enemyData.put("timeSinceLastMove",   timeSinceLastMove);
        enemyData.put("attackAnimationRunning", attackAnimationRunning);
        enemyData.put("attackAnimationElapsedTime", attackAnimationElapsedTime);
        enemyData.put("timeSinceTookDamage", timeSinceTookDamage);
        enemyData.put("inTookDamageAnimation", inTookDamageAnimation);
        enemyData.put("stateBeforeHurt", stateBeforeHurt.name());
        Log.d("targetcell", String.valueOf(targetCell==null));
        if(targetCell!=null)
            enemyData.put("targetCellPos", targetCell.getPosition().toMap());
        return enemyData;
    }

    public void setPath(List<Position> path) {
        this.path = path;
        this.currentTargetIndex = 0;
    }

    public void setState(EnemyState enemyState) {
        this.state = enemyState;
        if(enemyState == EnemyState.ATTACKING1 || enemyState == EnemyState.ATTACKING2 || enemyState == EnemyState.ATTACKING3 || enemyState == EnemyState.ATTACKING4)
            attackAnimationRunning = true;
    }

    public float getTimeSinceLastMove() {
        return timeSinceLastMove;
    }

    public void setTimeSinceLastMove(float timeSinceLastMove) {
        this.timeSinceLastMove = timeSinceLastMove;
    }

    public int getCurrentTargetIndex() {
        return currentTargetIndex;
    }

    public void setCurrentTargetIndex(int currentTargetIndex) {
        this.currentTargetIndex = currentTargetIndex;
    }

    public void setTimeSinceLastAttack(long timeSinceLastAttack) {
        this.timeSinceLastAttack = timeSinceLastAttack;
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public void setCurrentDirection(Direction currentDirection) {
        this.currentDirection = currentDirection;
    }

    public float getMovementSpeed() {
        return movementSpeed;
    }

    public EnemyState getEnemyState() {
        return state;
    }

    public void setAttackAnimationElapsedTime(long attackAnimationElapsedTime) {
        this.attackAnimationElapsedTime = attackAnimationElapsedTime;
    }

    public void setAttackAnimationRunning(boolean attackAnimationRunning) {
        this.attackAnimationRunning = attackAnimationRunning;
    }

    public void setInTookDamageAnimation(boolean inTookDamageAnimation) {
        this.inTookDamageAnimation = inTookDamageAnimation;
    }

    public void setTimeSinceTookDamage(long timeSinceTookDamage) {
        this.timeSinceTookDamage = timeSinceTookDamage;
    }

    public void setStateBeforeHurt(EnemyState stateBeforeHurt) {
        this.stateBeforeHurt = stateBeforeHurt;
    }

    public void setTargetCell(Cell cell) {
        this.targetCell = cell;
    }

    public int getReward() {
        return reward;
    }

    public List<Position> getPath() {
        return path;
    }
}