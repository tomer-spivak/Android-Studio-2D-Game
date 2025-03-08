package tomer.spivak.androidstudio2dgame.modelObjects;


import android.util.Log;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tomer.spivak.androidstudio2dgame.model.AttackComponent;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.Direction;
import tomer.spivak.androidstudio2dgame.modelEnums.EnemyState;
import tomer.spivak.androidstudio2dgame.modelEnums.EnemyType;

public class Enemy extends ModelObject implements IDamager{
    protected final float movementSpeed;
    protected Direction currentDirection;
    protected EnemyState state;
    private final EnemyType type;
    protected List<Position> path;
    protected int currentTargetIndex = 0;
    protected float timeSinceLastMove = 0;
    private final AttackComponent attackComponent;

    public Enemy(float health, float damage, float movementSpeed, Position pos,
                 EnemyType enemyType, float attackCooldown) {
        super(health, pos); // Call base constructor
        attackComponent = new AttackComponent(damage, attackCooldown);
        this.movementSpeed = movementSpeed;
        this.type = enemyType;
        this.currentDirection = Direction.UPLEFT;
        this.state = EnemyState.IDLE;
    }

    public void accumulateAttackTime(long deltaTime) {
        attackComponent.accumulateAttackTime(deltaTime);
    }

    public boolean canAttack() {
        return attackComponent.canAttack() && state != EnemyState.ATTACKING1 && state !=
                EnemyState.ATTACKING2 && state != EnemyState.ATTACKING3 &&
                state != EnemyState.HURT;
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

    @Override
    public void dealDamage(IDamageable target) {
        attackComponent.dealDamage(target);
    }

    public void attack(Building building) {
        // Use a Handler to post a delayed task to the main thread (if needed)
        // Use a Handler to post a delayed task to the main thread (if needed)
        dealDamage(building);
        setState(EnemyState.ATTACKING1);

        // Schedule a task to reset the state after 200ms
        int dev_time = 150;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Timer timer = new Timer();
                setState(EnemyState.ATTACKING2);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        setState(EnemyState.ATTACKING3);
                        // After 200ms, reset the state and the attack timer.
                        // Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                setState(EnemyState.ATTACKING4);
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        setState(EnemyState.ATTACKING3);
                                        timer.schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                setState(EnemyState.ATTACKING4);
                                                timer.schedule(new TimerTask() {
                                                    @Override
                                                    public void run() {
                                                        setState(EnemyState.ATTACKING3);
                                                        timer.schedule(new TimerTask() {
                                                            @Override
                                                            public void run() {
                                                                setState(EnemyState.ATTACKING4);
                                                                timer.schedule(new TimerTask() {
                                                                    @Override
                                                                    public void run() {
                                                                        setState(EnemyState.ATTACKING3);
                                                                        timer.schedule(new TimerTask() {
                                                                            @Override
                                                                            public void run() {
                                                                                setState(EnemyState.ATTACKING4);
                                                                                timer.schedule(new TimerTask() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        setState(EnemyState.IDLE);
                                                                                        resetAttackTimer();
                                                                                    }
                                                                                }, dev_time);
                                                                            }
                                                                        }, dev_time);
                                                                    }
                                                                }, dev_time);
                                                            }
                                                        }, dev_time);
                                                    }
                                                }, dev_time);
                                            }
                                        }, dev_time);
                                    }
                                }, dev_time);
                            }
                        }, dev_time);
                    }
                }, 200);
            }
        }, 200);

    }




    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);
        Log.d("take", String.valueOf(timeSinceLastMove));
        //timeSinceLastMove = 1000 / movementSpeed;
        //timeSinceLastMove += 1000;
        if (health <= 0){
            onDeath();
            return;
        }
        setState(EnemyState.HURT);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setState(EnemyState.IDLE);
            }
        }, 200);
    }

    public List<Position> getPath() {
        return path;
    }
    public void setPath(List<Position> path) {
        this.path = path;
        this.currentTargetIndex = 0;
        //this.timeSinceLastMove = 0;
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

    public EnemyType getType() {
        return type;
    }

    public EnemyState getEnemyState() {
        return state;
    }
    public void setState(EnemyState enemyState) {
        Log.d("state", enemyState.toString());
        this.state = enemyState;
    }

    @Override
    public Object toMap() {
        Map enemyData = (Map) super.toMap();
        enemyData.put("attackDamage", attackComponent.getAttackDamage());
        enemyData.put("movementSpeed", movementSpeed);
        enemyData.put("type", type.name());
        return enemyData;
    }
}