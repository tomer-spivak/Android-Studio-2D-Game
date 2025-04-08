package tomer.spivak.androidstudio2dgame.gameObjects;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.model.Position;

public class GameObjectFactory {
    private static final Map<String, GameObjectCreator> typeMap = new HashMap<>();

    static {
        typeMap.put("obelisk", GameBuilding::new);
        typeMap.put("lightningtower", GameBuilding::new);
    }


    public static GameObject create(Context context, Point point, String type, float scale,
                                    Position pos, String state, String direction) {
        if (type.equals("monster")) {
            return new GameEnemy(context, point, type, scale, pos, direction, state);
        }

        Log.d("building", "state: " + state);

        GameObjectCreator creator = typeMap.get(type);
        if (creator != null) {
            return creator.create(context, point, type, scale, pos, state);
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }

    @FunctionalInterface
    interface GameObjectCreator {
        GameObject create(Context context, Point point, String type, float scale,
                          Position pos, String buildingState);
    }
}
