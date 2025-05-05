package tomer.spivak.androidstudio2dgame.modelObjects;


import android.util.Log;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tomer.spivak.androidstudio2dgame.model.AttackComponent;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.Direction;
import tomer.spivak.androidstudio2dgame.modelEnums.EnemyState;

public class Enemy extends ModelObject implements IDamager{
    protected final float movementSpeed;
    protected Direction currentDirection;
    protected EnemyState state;
    protected List<Position> path;
    protected int currentTargetIndex = 0;
    protected float timeSinceLastMove = 0;
    private final AttackComponent attackComponent;
    private final int reward;
    private long attackAnimationElapsedTime;
    private boolean attackAnimationRunning = false;
    private Cell targetCell;

    public Enemy(float health, float damage, float movementSpeed, Position pos, float attackCooldown, int reward) {
        super(health, pos);
        this.reward = reward;
        attackComponent = new AttackComponent(damage, attackCooldown);
        this.movementSpeed = movementSpeed;
        this.currentDirection = Direction.UPLEFT;
        this.state = EnemyState.IDLE;
        this.type = "monster";
        attackComponent.setAttackDamage(damage/10);
    }

    public void accumulateAttackTime(long deltaTime) {
        if (state == EnemyState.ATTACKING1 || state == EnemyState.ATTACKING2 || state == EnemyState.ATTACKING3 ||
                state == EnemyState.ATTACKING4)
            return;
        attackComponent.accumulateAttackTime(deltaTime);
    }

    public boolean canAttack() {
        return attackComponent.canAttack() && (state == EnemyState.IDLE);
    }

    public void updateDirection(Position prevPos, Position nextPos) {
        Direction chosen;
        if (prevPos.getX() > nextPos.getX()){
            chosen = Direction.UPRIGHT;
        } else if(prevPos.getX() < nextPos.getX()){
            chosen = Direction.DOWNLEFT;
        } else {
            if (nextPos.getY() < prevPos.getY()){
                chosen = Direction.UPLEFT;
            } else
                chosen = Direction.DOWNRIGHT;
        }
        setCurrentDirection(chosen);

    }

    @Override
    public void dealDamage(IDamageable target) {
        attackComponent.dealDamage(target);
    }

    public void attack(Cell targetCell) {
        setSoundStreamId(soundEffects.playEnemyAttackSound());
        executeAttackAnimation(targetCell);
    }

    private void executeAttackAnimation(Cell targetCell) {
        this.targetCell = targetCell;

        resetAttackTimer();
        setState(EnemyState.ATTACKING1);
        attackAnimationElapsedTime = 0;
        attackAnimationRunning = true;
    }

    public void update(long deltaTime){
        if (attackAnimationRunning) {
            updateAnimation(deltaTime);
        }

    }

    public void updateAnimation(long deltaTime) {

        attackAnimationElapsedTime += deltaTime;

        int initDelay = 500;
        int stepDelay = 150;
        if (attackAnimationElapsedTime < initDelay) {
            setState(EnemyState.ATTACKING1);
        } else if (attackAnimationElapsedTime < initDelay + initDelay) {
            setState(EnemyState.ATTACKING2);
        } else if (attackAnimationElapsedTime < initDelay + initDelay + (20 * stepDelay)) {
            long t = attackAnimationElapsedTime - 2 * initDelay;
            long cycleTime = t % (2 * stepDelay);

            if (cycleTime < stepDelay) {
                setState(EnemyState.ATTACKING3);
            } else {
                setState(EnemyState.ATTACKING4);
                dealDamage(targetCell.getObject());
            }
        } else {
            setState(EnemyState.IDLE);
            attackAnimationRunning = false;
            resetAttackTimer();
        }
    }

    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);
        if (health <= 0){
            onDeath();
            return;
        }
        EnemyState currentState = getEnemyState();
        setState(EnemyState.HURT);
        boolean save = attackAnimationRunning;
        attackAnimationRunning = false;
        soundEffects.pauseSoundEffect(soundStreamId);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                attackAnimationRunning = save;
                setState(currentState);
                soundEffects.resumeSoundEffect(soundStreamId);
            }
        }, 1000);
    }

    public int getReward() {
        return reward;
    }

    public List<Position> getPath() {
        return path;
    }

    public void setPath(List<Position> path) {
        this.path = path;
        this.currentTargetIndex = 0;
    }

    public float getAccumulatedTime() {
        return timeSinceLastMove;
    }
    public void accumulateTime(long elapsedTime) {
        timeSinceLastMove += elapsedTime;
    }
    public void decreaseAccumulatedTime(float time) {
        timeSinceLastMove -= time;
    }
    public void setTimeSinceLastMove(float timeSinceLastMove) {
        this.timeSinceLastMove = timeSinceLastMove;
    }

    public int getCurrentTargetIndex() {
        return currentTargetIndex;
    }
    public void incrementTargetIndex(){
        currentTargetIndex++;
    }
    public void setCurrentTargetIndex(int currentTargetIndex) {
        this.currentTargetIndex = currentTargetIndex;
    }


    public void setTimeSinceLastAttack(float timeSinceLastAttack) {
        attackComponent.setTimeSinceLastAttack(timeSinceLastAttack);
    }
    public void resetAttackTimer() {
        attackComponent.resetAttackTimer();
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

    public void setState(EnemyState enemyState) {
        this.state = enemyState;
    }

    public void setAttackAnimationElapsedTime(long attackAnimationElapsedTime) {
        this.attackAnimationElapsedTime = attackAnimationElapsedTime;
    }

    public void setAttackAnimationRunning(boolean attackAnimationRunning) {
        this.attackAnimationRunning = attackAnimationRunning;
    }

    @Override
    public Object toMap() {
        Map enemyData = (Map) super.toMap();
        enemyData.put("type", "monster");
        enemyData.put("currentDirection", currentDirection.name());
        enemyData.put("enemyState",   state.name());
        enemyData.put("currentTargetIndex", currentTargetIndex);
        enemyData.put("timeSinceLastAttack", attackComponent.getTimeSinceLastAttack());
        enemyData.put("timeSinceLastMove",   timeSinceLastMove);
        enemyData.put("damage",              attackComponent.getAttackDamage());
        enemyData.put("movementSpeed",       movementSpeed);
        enemyData.put("reward",              reward);
        enemyData.put("attackAnimationRunning", attackAnimationRunning);
        enemyData.put("attackAnimationElapsedTime", attackAnimationElapsedTime);
        Log.d("targetcell", String.valueOf(targetCell==null));
        if(targetCell!=null)
            enemyData.put("targetCellPos", targetCell.getPosition().toMap());
        
        return enemyData;
    }

    public void setTargetCell(Cell cell) {
        this.targetCell = cell;
    }
}