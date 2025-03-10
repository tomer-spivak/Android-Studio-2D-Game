package tomer.spivak.androidstudio2dgame.gameObjects;

import static androidx.core.util.TypedValueCompat.pxToDp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import tomer.spivak.androidstudio2dgame.model.Position;

public class GameBuilding extends GameObject {
    public GameBuilding(Context context, Point point, String name, float scale, Position pos,
                        String building_state) {
        super(context, point, name, scale, pos);
        Log.d("building", name);
        Log.d("building", "building state: " + building_state);
        Log.d("building", imageResourceString);
        imageResourceString = imageResourceString + "_" + building_state;
        Log.d("building", imageResourceString);
        createView();
        setScaledSize();
    }

    private void setScaledSize() {
        this.scaledSize[0] = (int) pxToDp((float) (originalSize[0] * scale * 0.5), context.getResources().getDisplayMetrics());
        this.scaledSize[1] = (int) pxToDp((float) (originalSize[1] * scale * 0.5), context.getResources().getDisplayMetrics());

    }
    @Override
    public void setScale(float scale) {
        this.scale = scale;

        this.scaledSize[0] = (int) pxToDp((float) (originalSize[0] * scale * 0.5), context.getResources().getDisplayMetrics());
        this.scaledSize[1] = (int) pxToDp((float) (originalSize[1] * scale * 0.5), context.getResources().getDisplayMetrics());

    }

    public void drawView(Canvas canvas) {
        Bitmap scaledBitmap = createScaledBitmap();

        int topLeftX = imagePoint.x - (int) ((float) scaledSize[0] / 2);
        int topLeftY = imagePoint.y - scaledSize[1]/2;
        if (this.imageResourceString.equals("obelisk")){
            topLeftY = (int) (imagePoint.y - (double) scaledSize[1] /2 * 1.4);
        }
        if (this.imageResourceString.equals("archertower")){
            topLeftY = (int) (imagePoint.y - (double) scaledSize[1] /2 * 1.4);
        }

        canvas.drawBitmap(scaledBitmap, topLeftX, topLeftY, null);
    }





}
