package tomer.spivak.androidstudio2dgame.GridView;

import android.content.Context;
import android.graphics.Canvas;

import android.graphics.Point;

import androidx.core.content.ContextCompat;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.modelEnums.CellState;

public class DrawGridView {
    private final GridBitmap[] grasses;
    Context context;

    public DrawGridView(Context context) {
        this.grasses = new GridBitmap[CellState.values().length];
        grasses[0] = new GridBitmap(ContextCompat.getDrawable(context, R.drawable.grass_default),
                context);
        grasses[1] = new GridBitmap(ContextCompat.getDrawable(context, R.drawable.grass_hurt),
                context);
        grasses[2] = new GridBitmap(ContextCompat.getDrawable(context, R.drawable.grass_enemydeath1),
                context);
        grasses[3] = new GridBitmap(ContextCompat.getDrawable(context,
                R.drawable.grass_enemydeath2),
                context);
        grasses[4] = new GridBitmap(ContextCompat.getDrawable(context,
                R.drawable.grass_enemydeath3),
                context);
        this.context = context;


    }



    public void draw(Canvas canvas, Point cellCenter, float scale, CellState cellState) {
        GridBitmap bitmap;
        if (cellState == null)
            bitmap = grasses[0];
        else
            bitmap = grasses[cellState.ordinal()];
        bitmap.updateScale(scale);

        float topLeftGrassX = cellCenter.x - bitmap.getWidth() / 2;
        float topLeftGrassY = cellCenter.y - bitmap.getHeight() / 2;
        canvas.drawBitmap(bitmap.getBitmap(), topLeftGrassX, topLeftGrassY, null);
    }



}
