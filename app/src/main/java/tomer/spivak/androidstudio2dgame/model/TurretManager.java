package tomer.spivak.androidstudio2dgame.model;

import java.util.ArrayList;
import java.util.List;

import tomer.spivak.androidstudio2dgame.logic.Cell;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.modelObjects.Turret;

public class TurretManager {

    public void updateTurrets(GameState current, List<Enemy> enemies, long deltaTime) {
        List<Turret> turrets = getTurrets(current);
        for (Turret turret : turrets) {
            turret.update(deltaTime);
                turret.updateCellsToAttack(current);
                if (turret.shouldExecuteAttack(enemies)){
                    List<Position> positionsToAttack = turret.getCellsToAttack();
                    attackCells(current, (ArrayList<Position>) positionsToAttack);
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

    private void attackCells(GameState current, ArrayList<Position> positionsToAttack) {
        for (Position pos : positionsToAttack){
            Cell cell = current.getCellAt(pos);
            cell.executeBurntAnimation();
        }
    }


}
