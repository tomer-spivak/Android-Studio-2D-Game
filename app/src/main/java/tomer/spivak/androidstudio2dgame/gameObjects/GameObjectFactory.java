package tomer.spivak.androidstudio2dgame.gameObjects;

import android.content.Context;
import android.graphics.Point;

import java.util.HashMap;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.model.Position;

public class GameObjectFactory {
    private static final Map<String, GameObjectCreator> typeMap = new HashMap<>();

    static {
        typeMap.put("obelisk", GameObject::new);
        typeMap.put("lightningtower", GameObject::new);
    }


    public static GameObject create(Context context, Point point, String type, float scale, Position pos, String state, String direction) {
        if (type.equals("monster")) {
            return new GameObject(context, point, scale, pos, type, state, direction);
        }

        GameObjectCreator creator = typeMap.get(type);
        if (creator != null) {
            return creator.create(context, point, scale, pos, type, state, null);
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }

    @FunctionalInterface
    interface GameObjectCreator {
        GameObject create(Context context, Point point, float scale, Position pos, String type, String state, String direction);
    }
}
