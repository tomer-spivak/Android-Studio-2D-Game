package tomer.spivak.androidstudio2dgame.modelAnimations;

import java.util.Timer;
import java.util.TimerTask;

import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.modelEnums.AttackType;
import tomer.spivak.androidstudio2dgame.modelEnums.CellState;

public class CellAnimationManager {
    public static void executeAttackedAnimation(Cell cell, AttackType attackType) {
        if (attackType == AttackType.LIGHTNING)
            executeBurntAnimation(cell);

    }

    private static void executeBurntAnimation(Cell cell) {
        cell.setState(CellState.BURNT);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // After 200ms, reset the state and the attack timer.
                cell.setState(CellState.NORMAL);
            }
        }, 200);
    }
}
