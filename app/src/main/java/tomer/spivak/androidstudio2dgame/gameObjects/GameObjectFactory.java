package tomer.spivak.androidstudio2dgame.gameObjects;

import android.content.Context;
import android.graphics.Point;

import java.util.HashMap;
import java.util.Map;

public class GameObjectFactory {
    private static final Map<String, GameObjectCreator> typeMap = new HashMap<>();

    static {
        typeMap.put("tower", GameBuilding::new);
        typeMap.put("monster", GameEnemy::new);
    }

    public static GameObject create(Context context, Point point, String type, float scale) {
        GameObjectCreator creator = typeMap.get(type);
        if (creator != null) {
            return creator.create(context, point, type, scale);
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }

    @FunctionalInterface
    interface GameObjectCreator {
        GameObject create(Context context, Point point, String type, float scale);
    }
}

