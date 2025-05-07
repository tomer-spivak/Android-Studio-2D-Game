package tomer.spivak.androidstudio2dgame.graphics;

import static androidx.core.util.TypedValueCompat.pxToDp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;


import tomer.spivak.androidstudio2dgame.model.Position;

public class GameObject {
    protected final ImageView imageView;
    protected Point imagePoint;
    protected Context context;
    protected float scale;
    protected int[] scaledSize;
    protected int[] originalSize;
    protected Drawable drawable;
    protected String imageResourceString;
    protected Position pos;
    protected String type;
    private float healthPercentage;
    private String state, direction;
    private static final float FIX_SIZE = 1.2f;
    private static final float FIX_WIDTH_MAINBUILDING = 1.5F;
    private static final float FIX_HEIGHT_MAINBUILDING = 1.1F;
    private static final float MAINBUILDING_OFFSET = 0.4f;
    private static final float BUILDING_OFFSET = 0.75f;


    public GameObject(Context context, Point imagePoint, float scale, Position pos, String type, String state, String direction, float healthPercentage) {
        this.context = context;
        imageView = new ImageView(context);
        this.imagePoint = imagePoint;
        this.scale = scale;
        this.pos = pos;
        imageResourceString = type.toLowerCase() + "_" + state;
        if (type.equals("monster")) {
            imageResourceString += "_" + direction;
        }
        scaledSize = new int[2];
        originalSize = new int[2];
        this.type = type;
        this.healthPercentage = healthPercentage;
        this.state  = state;
        this.direction = direction;
        createImageView();
        setScale(scale);
    }

    protected void createImageView() {
        imageView.setImageResource(context.getResources().getIdentifier(imageResourceString, "drawable", context.getPackageName()));

        if (imageView.getDrawable() == null) {
            return;
        }

        drawable = imageView.getDrawable();

        this.originalSize[0] = drawable.getIntrinsicWidth();
        this.originalSize[1] = drawable.getIntrinsicHeight();

        if(imageResourceString.contains("building")){
            scaledSize[0] = (int) pxToDp(originalSize[0] * scale * FIX_WIDTH_MAINBUILDING, context.getResources().getDisplayMetrics());
            scaledSize[1] = (int) pxToDp(originalSize[1] * scale * FIX_HEIGHT_MAINBUILDING, context.getResources().getDisplayMetrics());
        } else{
            scaledSize[0] = (int) pxToDp(originalSize[0] * scale * FIX_SIZE, context.getResources().getDisplayMetrics());
            scaledSize[1] = (int) pxToDp(originalSize[1] * scale * FIX_SIZE, context.getResources().getDisplayMetrics());
        }
    }

    public void drawView(Canvas canvas) {
        Bitmap scaledBitmap;
        if (drawable instanceof BitmapDrawable) {
            scaledBitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        else {
            Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledSize[0], scaledSize[1], true);
        }
        int topLeftX = imagePoint.x - (int) ((float) scaledSize[0] / 2);
        int topLeftY = imagePoint.y - scaledSize[1]/2;
        if (imageResourceString.contains("mainbuilding")){
            topLeftY = imagePoint.y - (int) (scaledSize[1] * MAINBUILDING_OFFSET);
        } else if (!imageResourceString.contains("monster")) {
            topLeftY = imagePoint.y - (int) (scaledSize[1] * BUILDING_OFFSET);
        }
        canvas.drawBitmap(scaledBitmap, topLeftX, topLeftY, null);
    }

    public void setScale(float scale) {
        this.scale = scale;
        if(imageResourceString.contains("mainbuilding")){
            this.scaledSize[0] = (int) pxToDp(originalSize[0] * scale * FIX_WIDTH_MAINBUILDING, context.getResources().getDisplayMetrics());
            this.scaledSize[1] = (int) pxToDp(originalSize[1] * scale * FIX_HEIGHT_MAINBUILDING, context.getResources().getDisplayMetrics());
        } else{
            this.scaledSize[0] = (int) pxToDp(originalSize[0] * scale * FIX_SIZE, context.getResources().getDisplayMetrics());
            this.scaledSize[1] = (int) pxToDp(originalSize[1] * scale * FIX_SIZE, context.getResources().getDisplayMetrics());
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

    public String getType() {
        return type;
    }

    public void updateState(String newState, String newDirection, float newHealthPercentage) {
        this.state = newState;
        this.direction = newDirection;
        this.healthPercentage = newHealthPercentage;

        if (type.equals("monster")) {
            imageResourceString = type + "_" + state + "_" + direction;
        } else {
            imageResourceString = type + "_" + state;
        }
        createImageView();
        setScale(this.scale);
    }

    public float getHealthPercentage() {
        return healthPercentage;
    }
    public int getScaledWidth() {
        return scaledSize[0];
    }
    public int getScaledHeight() {
        return scaledSize[1];
    }

    public float getScale() {
        return scale;
    }
}
