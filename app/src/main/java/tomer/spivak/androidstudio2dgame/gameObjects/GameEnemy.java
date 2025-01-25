package tomer.spivak.androidstudio2dgame.gameObjects;

import android.content.Context;
import android.graphics.Point;

public class GameEnemy extends GameObject{
    GameBuilding buildingToAttack;

    public GameEnemy(Context context, Point point, int imageUrl, String name){
        super(context, point, imageUrl, name);
    }



    @Override
    public void update() {

    }

    public void setAttackingBuilding(GameBuilding building) {
        buildingToAttack = building;
    }
}
