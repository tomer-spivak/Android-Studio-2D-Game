package tomer.spivak.androidstudio2dgame.graphics;

import static androidx.core.util.TypedValueCompat.pxToDp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;


import tomer.spivak.androidstudio2dgame.logic.Position;

public class GameObject {
    private Point imagePoint;
    private final Context context;
    private float scale;
    private final int[] scaledSize;
    private final int[] originalSize;
    private Drawable drawable;
    private String imageResourceString;
    private final Position pos;
    private final String type;
    private float healthPercentage;

    public GameObject(Context context, Point point, float scale, Position pos, String type, String state, String direction, float healthPercentage) {
        this.context = context;
        this.imagePoint = point;
        this.scale = scale;
        this.pos = pos;
        this.healthPercentage = healthPercentage;
        imageResourceString = type.toLowerCase() + "_" + state;
        if ("monster".contains(type)) {
            imageResourceString += "_" + direction;
        }
        scaledSize = new int[2];
        originalSize = new int[2];
        this.type = type;
        createView();
        setScale(scale);
    }

    private void createView() {
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(context.getResources().getIdentifier(imageResourceString, "drawable", context.getPackageName()));

        if (imageView.getDrawable() == null) {
            return;
        }

        drawable = imageView.getDrawable();

        int originalWidth = drawable.getIntrinsicWidth();
        int originalHeight = drawable.getIntrinsicHeight();

        this.originalSize[0] = originalWidth;
        this.originalSize[1] = originalHeight;
        if(imageResourceString.contains("mainbuilding")){
            scaledSize[0] = (int) pxToDp((float) (originalWidth * scale * 1.5), context.getResources().getDisplayMetrics());
            scaledSize[1] = (int) pxToDp((float) (originalHeight * scale * 1.15), context.getResources().getDisplayMetrics());
        } else{
            scaledSize[0] = (int) pxToDp((float) (originalWidth * scale * 1.2), context.getResources().getDisplayMetrics());
            scaledSize[1] = (int) pxToDp((float) (originalHeight * scale * 1.2), context.getResources().getDisplayMetrics());
        }
    }

    public void drawView(Canvas canvas) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            Canvas temporaryCanvas = new Canvas();
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            drawable.setBounds(0, 0, temporaryCanvas.getWidth(), temporaryCanvas.getHeight());
            drawable.draw(temporaryCanvas);
        }

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledSize[0], scaledSize[1], true);
        int topLeftX = imagePoint.x - (int) ((float) scaledSize[0] / 2);
        int topLeftY = imagePoint.y - scaledSize[1]/2;
        if (imageResourceString.contains("lightning") || imageResourceString.contains("obelisk") || imageResourceString.contains("exploding")) {
            topLeftY = imagePoint.y - (int) (scaledSize[1] * 0.75);
        }
        if (imageResourceString.contains("mainbuilding")){
            topLeftY = imagePoint.y - (int) (scaledSize[1] * 0.45);
        }
        canvas.drawBitmap(scaledBitmap, topLeftX, topLeftY, null);
    }

    public void setScale(float scale) {
        this.scale = scale;
        if(imageResourceString.contains("mainbuilding")){
            this.scaledSize[0] = (int) pxToDp((float) (originalSize[0] * scale * 1.5), context.getResources().getDisplayMetrics());
            this.scaledSize[1] = (int) pxToDp((float) (originalSize[1] * scale * 1.15), context.getResources().getDisplayMetrics());
        } else{
            this.scaledSize[0] = (int) pxToDp((float) (originalSize[0] * scale * 1.2), context.getResources().getDisplayMetrics());
            this.scaledSize[1] = (int) pxToDp((float) (originalSize[1] * scale * 1.2), context.getResources().getDisplayMetrics());
        }
    }

    public Point getImagePoint() {
        return imagePoint;
    }

    public Position getPos() {
        return pos;
    }

    public void setImagePoint(Point point) {
        this.imagePoint = point;
    }

    public float getScale() {
        return scale;
    }

    public float getHealthPercentage() {
        return healthPercentage;
    }

    public int[] getScaledSize() {
        return scaledSize;
    }

    public String getType() {
        return type;
    }

    public void updateState(String state, String direction, float healthPercentage) {
        this.healthPercentage = healthPercentage;
        imageResourceString = type.toLowerCase() + "_" + state;
        if ("monster".contains(type)) {
            imageResourceString += "_" + direction;
        }
        createView();
    }
}