package tomer.spivak.androidstudio2dgame.modelObjects;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
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
        executeAttackAnimation();
        dealDamage(building);
    }

    private void executeAttackAnimation() {
        // Define the initial states
        final EnemyState[] initialStates = {
                EnemyState.ATTACKING1,
                EnemyState.ATTACKING2,
        };

        // Define the repeated alternating pattern: ATTACKING4 followed by ATTACKING3.
        // Adjust the repeatCount to match how many times you want the pattern to occur.
        final int repeatCount = 3;
        final List<EnemyState> stateSequence = new ArrayList<>();
        final List<Integer> delays = new ArrayList<>();
        delays.add(0);


        // Add the initial sequence
        Collections.addAll(stateSequence, initialStates);

        int initDelay = 500;
        delays.add(initDelay);
        delays.add(initDelay);


        int stepDelay = 150;

        // Add the repeating pattern
        for (int i = 0; i < repeatCount; i++) {
            stateSequence.add(EnemyState.ATTACKING3);
            delays.add(stepDelay);
            stateSequence.add(EnemyState.ATTACKING4);
            delays.add(stepDelay);
        }

        // Finally, add the final state to reset the enemy
        stateSequence.add(EnemyState.IDLE);
        delays.add(stepDelay);

        // Define corresponding delays (in milliseconds) between state changes.
        // You can customize these values as needed.

        // Use a Handler to schedule the state changes on the main thread
        final Handler handler = new Handler(Looper.getMainLooper());
        int accumulatedDelay = 0;

        for (int i = 0; i < stateSequence.size(); i++) {
            final EnemyState state = stateSequence.get(i);
            accumulatedDelay += delays.get(i);
            handler.postDelayed(() -> {
                setState(state);
                // When reaching the final state, also reset the attack timer.
                if (state == EnemyState.IDLE) {
                    resetAttackTimer();
                }
            }, accumulatedDelay);
        }
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