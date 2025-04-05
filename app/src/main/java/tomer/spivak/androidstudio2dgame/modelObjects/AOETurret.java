package tomer.spivak.androidstudio2dgame.modelObjects;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.AttackType;
import tomer.spivak.androidstudio2dgame.modelEnums.TurretType;

public class AOETurret extends Turret{
    List<Position> positionsToAttack = new ArrayList<>();
    List<Position> removedPositions = new ArrayList<>();

    public AOETurret(float health, float attackDamage, float attackRange, Position pos,
                     TurretType type, long attackCooldown, AttackType attackType, int price) {
        super(health, attackDamage, attackRange, pos, type, attackCooldown, attackType, price);
        initCellsToAttack();
    }


    private void initCellsToAttack(){
        if (type == TurretType.LIGHTNINGTOWER){
            for (int i = 0; i < attackRange; i++){
                positionsToAttack.add(new Position(pos.getX() + i + 1, pos.getY()));
            }
            for (int i = 0; i < attackRange; i++){
                positionsToAttack.add(new Position(pos.getX() - i - 1, pos.getY()));
            }

            for (int i = 0; i < attackRange; i++){
                positionsToAttack.add(new Position(pos.getX(), pos.getY() + i + 1));
            }

            for (int i = 0; i < attackRange; i++){
                positionsToAttack.add(new Position(pos.getX(), pos.getY() - i - 1));
            }
        }
    }

    public void update(long elapsedTime){
        attackComponent.accumulateAttackTime(elapsedTime);

    }

    public boolean shouldExecuteAttack(List<Enemy> enemies) {
        boolean bool = false;
        if (!canAttack()){
            Log.d("AOE", "cant Attack");
            return false;
        }


        for (Enemy enemy : enemies){
            Log.d("AOE", "turret pos " + pos.toString());
            Log.d("AOE", "enemy pos" + enemy.getPosition().toString());
            for (Position pos : positionsToAttack){
                if (pos.equals(enemy.getPosition())){
                    executeAttackSoundAndAnimation(enemy);
                    attackComponent.resetAttackTimer();
                    bool = true;
                }
            }
        }
        return bool;
    }

    public List<Position> getCellsToAttack() {
        return positionsToAttack;
    }

    private boolean shouldAttackPosition(Position position, GameState current){
        return current.isValidPosition(position) && !(current.getCellAt(position).getObject() instanceof Building);
    }

    public void updateCellsToAttack(GameState current) {
        Iterator<Position> iterator = positionsToAttack.iterator();
        while (iterator.hasNext()) {
            Position pos = iterator.next();
            if (!shouldAttackPosition(pos, current)) {
                if(current.isValidPosition(pos))
                    removedPositions.add(pos);
                iterator.remove(); // ✅ safe removal
            }
        }

        // Re-check removedPositions
        Iterator<Position> removedIterator = removedPositions.iterator();
        while (removedIterator.hasNext()) {
            Position pos = removedIterator.next();
            Log.d("position", "addingToRemovedPositons:" + pos);
            Log.d("position", "re-checking:" + (current.getCellAt(pos).getObject() instanceof Building));
            if (shouldAttackPosition(pos, current)) {
                Log.d("position", "re-adding");
                positionsToAttack.add(pos);
                removedIterator.remove(); // ✅ safe removal from removedPositions
            }
        }
    }
}
