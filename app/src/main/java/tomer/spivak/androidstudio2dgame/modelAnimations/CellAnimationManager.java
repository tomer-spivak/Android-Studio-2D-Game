package tomer.spivak.androidstudio2dgame.modelAnimations;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    public static void executeEnemyDeathAnimation(Cell cell) {
        // Define the animation states for one cycle.
        final CellState[] states = {
                CellState.ENEMYDEATH1,
                CellState.ENEMYDEATH2,
                CellState.ENEMYDEATH3,
        };

        final int cyclesToRun = 3;
        final int stepsPerCycle = states.length;
        final int totalSteps = cyclesToRun * stepsPerCycle;
        final AtomicInteger stepCounter = new AtomicInteger(0);

        // Create a scheduler that will update the state every 200ms.
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                int step = stepCounter.getAndIncrement();
                if (step < totalSteps) {
                    // Determine the state for the current step.
                    int stateIndex = step % stepsPerCycle;
                    cell.setState(states[stateIndex]);
                } else {
                    cell.setState(CellState.NORMAL);
                    scheduler.shutdown();
                }
            }
        }, 0, 300, TimeUnit.MILLISECONDS);
    }
}
