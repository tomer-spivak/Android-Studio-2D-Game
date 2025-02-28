package tomer.spivak.androidstudio2dgame.modelObjects;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.TurretType;

public class AOETurret extends Turret{
    List<Position> positionsToAttack = new ArrayList<>();

    public AOETurret(float health, float attackDamage, float attackRange, Position pos,
                     TurretType type, long attackCooldown) {
        super(health, attackDamage, attackRange, pos, type, attackCooldown);
        setCellsToAttack();
    }


    private void setCellsToAttack(){
        if (type == TurretType.ARCHERTOWER){
            //square
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
            for (Position pos : positionsToAttack){
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
        return positionsToAttack;
    }

    public void updateCellsToAttack(GameState current) {
        positionsToAttack.removeIf(pos -> !current.isValidPosition(pos) || current.getCellAt(pos)
                .getObject() instanceof Building);
    }
}
