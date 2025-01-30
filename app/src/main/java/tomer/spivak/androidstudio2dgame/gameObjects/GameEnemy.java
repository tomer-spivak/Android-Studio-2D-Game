package tomer.spivak.androidstudio2dgame.gameObjects;

import static androidx.core.util.TypedValueCompat.pxToDp;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import java.util.Arrays;

import tomer.spivak.androidstudio2dgame.logic.Enemy;
import tomer.spivak.androidstudio2dgame.logic.Monster;

public class GameEnemy extends GameObject{
    GameBuilding buildingToAttack;
    Enemy enemy;
    
    
    public GameEnemy(Context context, Point point, String name, float scale){
        super(context, point, name, scale);
        if (name.equals("monster")){
            enemy = new Monster();
        }
        Log.d("debug", "creating new enemy: " + Arrays.toString(scaledSize));
        setScaledSize();
    }
    private void setScaledSize(){
        Log.d("debug", Arrays.toString(originalSize));
        this.scaledSize[0] = (int) pxToDp((float) (originalSize[0] * scale * 0.6), context.getResources().getDisplayMetrics());
        this.scaledSize[1] = (int) pxToDp((float) (originalSize[1] * scale * 0.6), context.getResources().getDisplayMetrics());

    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;

        Log.d("debug", "scale: " + scale);
        this.scaledSize[0] = (int) pxToDp((float) (originalSize[0] * scale * 0.6), context.getResources().getDisplayMetrics());
        this.scaledSize[1] = (int) pxToDp((float) (originalSize[1] * scale * 0.6), context.getResources().getDisplayMetrics());

    }



    @Override
    public void update() {

    }

//    public void setAttackingBuilding(GameBuilding building) {
//        buildingToAttack = building;
//        calculatePath();
//        Log.d("debug", "setAttackingBuilding: " + building.getName());
//    }

    private void calculatePath() {
    }
}
