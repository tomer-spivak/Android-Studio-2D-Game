package tomer.spivak.androidstudio2dgame.modelObjects;


import android.util.Log;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tomer.spivak.androidstudio2dgame.model.Position;

public abstract class Enemy extends ModelObject {
    protected final float damage;
    protected final float movementSpeed;
    protected Direction currentDirection;
    protected EnemyState enemyState;
    protected List<Position> path;
    protected int currentTargetIndex = 0;
    protected float timeSinceLastMove = 0;
    protected float timeSinceLastAttack = 0;
    private final float attackCooldown;


    public Enemy(float health, float damage, float movementSpeed, Position pos,
                 float attackCooldown) {
        super(health, pos); // Call base constructor
        this.damage = damage;
        this.movementSpeed = movementSpeed;
        this.attackCooldown = attackCooldown;
        this.currentDirection = Direction.UPLEFT;
        this.enemyState = EnemyState.IDLE;
    }

    @Override
    public void dealDamage(Damage target) {
        Log.d("attack", "enemy deals damage: " + damage);
        target.takeDamage(damage); // Attack logic
    }

    public List<Position> getPath() {
        return path;
    }

    public void setPath(List<Position> path) {
        this.path = path;
        this.currentTargetIndex = 0;
        //this.timeSinceLastMove = 0;
    }

    public int getCurrentTargetIndex() {
        return currentTargetIndex;
    }
    public void incrementTargetIndex(){
        currentTargetIndex++;
    }
    public void accumulateTime(long elapsedTime) {
        timeSinceLastMove += elapsedTime;
    }
    public float getAccumulatedTime() {
        return timeSinceLastMove;
    }
    public void decreaseAccumulatedTime(float time) {
        timeSinceLastMove -= time;
    }
    public void setCurrentDirection(Direction currentDirection) {
        this.currentDirection = currentDirection;
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public float getMovementSpeed() {
        return movementSpeed;
    }

    public void accumulateAttackTime(long deltaTime) {
        timeSinceLastAttack += deltaTime;
    }

    public EnemyState getEnemyState() {
        return enemyState;
    }

    public void setEnemyState(EnemyState enemyState) {
        this.enemyState = enemyState;
    }


    public boolean canAttack() {
        Log.d("attack", String.valueOf(timeSinceLastAttack));
        Log.d("attack", String.valueOf(attackCooldown));
        return timeSinceLastAttack >= attackCooldown;
    }

    public void resetAttackTimer() {
        timeSinceLastAttack = 0;
    }

    @Override
    public Object toMap() {
        Map enemyData = (Map) super.toMap();
        enemyData.put("damage", damage);
        enemyData.put("movementSpeed", movementSpeed);
        enemyData.put("type", "enemy");
        return enemyData;
    }

    public void updateDirection(Position prevPos) {
        updateDirection(prevPos, pos);
    }
    public void updateDirection(Position prevPos, Position nextPos) {
        if (prevPos.getX() > nextPos.getX()){
            setCurrentDirection(Direction.UPRIGHT);
        } else if(prevPos.getX() < nextPos.getX()){
            setCurrentDirection(Direction.DOWNLEFT);
        } else {
            if (nextPos.getY() < prevPos.getY()){
                setCurrentDirection(Direction.UPLEFT);
            } else
                setCurrentDirection(Direction.DOWNRIGHT);
        }
    }

    public void attack(Building building) {
        // Use a Handler to post a delayed task to the main thread (if needed)
        // Use a Handler to post a delayed task to the main thread (if needed)
        resetAttackTimer();

        dealDamage(building);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // After 200ms, reset the state and the attack timer.
                setEnemyState(EnemyState.IDLE);
                resetAttackTimer();
            }
        }, 1200);
    }
}