package tomer.spivak.androidstudio2dgame.modelObjects;


import android.util.Log;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tomer.spivak.androidstudio2dgame.model.AttackComponent;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelAnimations.EnemyAttackAnimation;
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
    private final EnemyAttackAnimation attackAnimation;
    private EnemyAttackAnimation activeAttackAnimation = null;
    private final int reward;


    public Enemy(float health, float damage, float movementSpeed, Position pos,
                 EnemyType enemyType, float attackCooldown, EnemyAttackAnimation attackAnimation, int reward) {
        super(health, pos); // Call base constructor
        this.reward = reward;
        attackComponent = new AttackComponent(damage, attackCooldown);
        this.movementSpeed = movementSpeed;
        this.type = enemyType;
        this.currentDirection = Direction.UPLEFT;
        this.state = EnemyState.IDLE;
        this.attackAnimation = attackAnimation;

        attackComponent.setAttackDamage(damage/attackAnimation.getRepeatCount());
    }

    public void accumulateAttackTime(long deltaTime) {
        if (state == EnemyState.ATTACKING1 || state == EnemyState.ATTACKING2 || state == EnemyState.ATTACKING3 || state == EnemyState.ATTACKING4)
            return;
        attackComponent.accumulateAttackTime(deltaTime);
    }

    public boolean canAttack() {
        Log.d("enemy", "attack time: " + attackComponent.getAttackTime());
        return attackComponent.canAttack() && (state == EnemyState.IDLE);
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
        Log.d("enemy", "enemy is attacking");
        setSoundStreamId(soundEffects.playEnemyAttackSound());
        activeAttackAnimation = attackAnimation;
        activeAttackAnimation.execute(this, building);
    }

    public void update(long deltaTime){
        if (state != EnemyState.HURT)
            updateAnimation(deltaTime);
    }

    public void updateAnimation(long deltaTime) {
        if (activeAttackAnimation != null && activeAttackAnimation.isRunning()) {
            activeAttackAnimation.update(deltaTime);
        }
    }

    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);
        Log.d("take", String.valueOf(timeSinceLastMove));

        if (health <= 0){
            onDeath();
            return;
        }
        EnemyState currentState = getEnemyState();
        setState(EnemyState.HURT);
        soundEffects.pauseSoundEffect(soundStreamId);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setState(currentState);
                soundEffects.resumeSoundEffect(soundStreamId);
            }
        }, 1000);
    }

    @Override
    void onDeath() {
        super.onDeath();
        if (activeAttackAnimation != null) {
            activeAttackAnimation.cancelAnimation();
            activeAttackAnimation = null;
        }
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
        Log.d("enemyState", enemyState.toString());
        this.state = enemyState;
    }

    @Override
    public Object toMap() {
        Map enemyData = (Map) super.toMap();
        enemyData.put("currentDirection", currentDirection.name());
        enemyData.put("enemyState", state.name());
        enemyData.put("currentTargetIndex", currentTargetIndex);
        enemyData.put("timeSinceLastAttack", attackComponent.getTimeSinceLastAttack());
        enemyData.put("timeSinceLastMove", timeSinceLastMove);
        enemyData.put("damage", attackComponent.getAttackDamage());
        enemyData.put("type", type.name());
        return enemyData;
    }

}