package tomer.spivak.androidstudio2dgame.model;

import java.util.ArrayList;
import java.util.List;

import tomer.spivak.androidstudio2dgame.modelEnums.AttackType;
import tomer.spivak.androidstudio2dgame.modelObjects.AOETurret;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.modelObjects.Turret;

public class TurretManager {
    public void updateTurrets(GameState current, List<Enemy> enemies, long deltaTime) {
        List<Turret> turrets = getTurrets(current);
        for (Turret turret : turrets) {
            if (turret instanceof AOETurret){
                AOETurret aoeTurret = (AOETurret) turret;
                if (aoeTurret.update(enemies, deltaTime)){
                    aoeTurret.updateCellsToAttack(current);
                    List<Position> positionsToAttack = aoeTurret.getCellsToAttack();
                    createCellsToAttack(current, (ArrayList<Position>) positionsToAttack,
                            turret.getAttackType());
                }
            }
        }
    }
    private List<Turret> getTurrets(GameState current) {
        List<Turret> turrets = new ArrayList<>();
        Cell[][] grid = current.getGrid();
        for (Cell[] row : grid) {
            for (Cell cell : row) {
                ModelObject obj = cell.getObject();
                if (obj instanceof Turret) {
                    turrets.add((Turret) obj);
                }
            }
        }
        return turrets;
    }
    private void createCellsToAttack(GameState current, ArrayList<Position> positionsToAttack,
                                     AttackType attackType) {
        for (Position pos : positionsToAttack){
            Cell cell = current.getCellAt(pos);
            cell.cellAttacked(attackType);
        }
    }


}
