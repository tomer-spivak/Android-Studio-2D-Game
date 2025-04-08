package tomer.spivak.androidstudio2dgame.modelAnimations;

import java.util.Timer;
import java.util.TimerTask;

import tomer.spivak.androidstudio2dgame.modelEnums.AttackType;
import tomer.spivak.androidstudio2dgame.modelEnums.BuildingState;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.Turret;

public class TurretAnimationManager {
    public static void executeAttackAnimation(Turret turret, Enemy target) {
        if (turret.getAttackType() == AttackType.LIGHTNING){
            executeLightningAttackAnimation(turret, target);
        }
    }

    private static void executeLightningAttackAnimation(Turret turret, Enemy target) {
        turret.setState(BuildingState.ATTACKING);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                turret.setState(BuildingState.IDLE);
                turret.dealDamage(target);
                turret.resetAttackTimer();
            }
        }, 200);
    }
}
