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
        // Register types that match the five-parameter constructor signature.
        typeMap.put("obelisk", GameBuilding::new);
        typeMap.put("archertower", GameBuilding::new);
        // Do not register "monster" here since GameEnemy requires an extra parameter.
    }


    public static GameObject create(Context context, Point point, String type, float scale,
                                    Position pos, String state, String direction) {
        if (type.equals("monster")) {
            // Handle GameEnemy separately since it needs the extra direction parameter.
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
