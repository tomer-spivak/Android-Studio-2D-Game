package tomer.spivak.androidstudio2dgame.modelObjects;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.BuildingState;
import tomer.spivak.androidstudio2dgame.modelEnums.RuinType;

public class Ruin extends Building{
    private final RuinType type;

    public Ruin(float health, Position pos, RuinType type) {
        super(health, pos);
        this.type = type;
    }



    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);
        setState(BuildingState.HURT);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setState(BuildingState.IDLE);
            }
        }, 200);
    }

    public RuinType getType() {
        return type;
    }



    @Override
    public Object toMap() {
        Map ruinData = (Map) super.toMap();
        ruinData.put("type", type.name());
        return ruinData;
    }


}
