package tomer.spivak.androidstudio2dgame.modelObjects;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.TurretType;

public class AOETurret extends Turret{
    List<Position> cellsToAttack = new ArrayList<>();

    public AOETurret(float health, float attackDamage, float attackRange, Position pos, TurretType type, long attackCooldown) {
        super(health, attackDamage, attackRange, pos, type, attackCooldown);
        setCellsToAttack();
    }


    private void setCellsToAttack(){
        if (type == TurretType.ARCHERTOWER){
            //square
            cellsToAttack = pos.getNeighbors();
        }
    }

    public boolean update(List<Enemy> enemies, long elapsedTime){
        attackComponent.accumulateAttackTime(elapsedTime);

        return isTargetInRange(enemies);
    }

    private boolean isTargetInRange(List<Enemy> enemies) {
        boolean bool = false;
        if (!canAttack()){

            Log.d("AOE", "cant Attack");
            return false;
        }


        for (Enemy enemy : enemies){
            Log.d("AOE", "turret pos " + pos.toString());
            Log.d("AOE", "enemy pos" + enemy.getPosition().toString());
            for (Position pos : cellsToAttack){
                if (pos.equals(enemy.getPosition())){
                    attack(enemy);
                    attackComponent.resetAttackTimer();
                    bool = true;
                }
            }
        }
        return bool;
    }

    public List<Position> getCellsToAttack() {
        return cellsToAttack;
    }
}
