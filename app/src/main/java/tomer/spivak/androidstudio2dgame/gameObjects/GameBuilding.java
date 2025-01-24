package tomer.spivak.androidstudio2dgame.gameObjects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;

public class GameBuilding extends GameObject {
    public GameBuilding(Context context, Point point, int imageUrl, String name) {
        super(context, point, imageUrl, name);
    }

    public void drawView(Canvas canvas) {
        Bitmap scaledBitmap = createScaledBitmap();

        int topLeftX = (int) (imagePoint.x - (int) ((float) scaledSize[0] / 2));
        int topLeftY = imagePoint.y - scaledSize[1]/2;
        if (this.name.equals("Tower")){
            topLeftY = (int) (imagePoint.y - (double) scaledSize[1] /2 * 1.4);
        }

        canvas.drawBitmap(scaledBitmap, topLeftX, topLeftY, null);
    }




    @Override
    public void update() {

    }
}
