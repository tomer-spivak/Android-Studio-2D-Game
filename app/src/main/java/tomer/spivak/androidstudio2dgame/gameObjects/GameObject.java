package tomer.spivak.androidstudio2dgame.gameObjects;

import static androidx.core.util.TypedValueCompat.pxToDp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;


import tomer.spivak.androidstudio2dgame.model.Position;


public class GameObject {
    protected ImageView view;
    protected Point imagePoint;
    protected Context context;
    protected float scale;
    protected int[] scaledSize;
    protected int[] originalSize;
    protected Drawable drawable;
    protected String imageResourceString;
    protected Position pos;
    protected String type;

    public GameObject(Context context, Point point, float scale, Position pos, String type, String state, String direction) {
        this.context = context;
        this.imagePoint = point;
        this.scale = scale;
        this.pos = pos;
        Log.d("type", "type: " + type);
        if ("monster".contains(type)) {
            imageResourceString = type.toLowerCase() + "_" + state + "_" + direction;
        } else
            imageResourceString = type.toLowerCase() + "_" + state;

        scaledSize = new int[2];
        originalSize = new int[2];
        this.type = type;

        if (!type.contains("monster"))
            this.type = "building";
        Log.d("type", "img: " + imageResourceString);
        Log.d("type", "dir" + direction);
        createView();
        setScale(scale);
    }


    protected void createView() {
        Log.d("grass", "creating new game object: " + imageResourceString);
        ImageView imageView = new ImageView(context); // Use your Activity or Application context
        imageView.setImageResource(context.getResources().getIdentifier(imageResourceString, "drawable", context.getPackageName()));
        this.view = imageView;

        if (view.getDrawable() == null) {
            return;
        }

        drawable = view.getDrawable();

        int originalWidth = drawable.getIntrinsicWidth();
        int originalHeight = drawable.getIntrinsicHeight();

        this.originalSize[0] = originalWidth;
        this.originalSize[1] = originalHeight;

        scaledSize[0] = (int) pxToDp((float) (originalWidth * scale * 1.2),
                context.getResources().getDisplayMetrics());
        scaledSize[1] = (int) pxToDp((float) (originalHeight * scale * 1.2),
                        context.getResources().getDisplayMetrics());
    }

    public void drawView(Canvas canvas) {
        Bitmap scaledBitmap = createScaledBitmap();
        int topLeftX = imagePoint.x - (int) ((float) scaledSize[0] / 2);
        int topLeftY = imagePoint.y - scaledSize[1]/2;
        if (!imageResourceString.contains("monster")) {
            topLeftY = (int) (imagePoint.y - ((double) scaledSize[1] / 2) * 1.2);
        }
        canvas.drawBitmap(scaledBitmap, topLeftX, topLeftY, null);
    }

    protected Bitmap createScaledBitmap() {
        return Bitmap.createScaledBitmap(
                drawableToBitmap(drawable),
                scaledSize[0],
                scaledSize[1],
                true
        );
    }

    protected Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void setScale(float scale) {
        this.scale = scale;
        this.scaledSize[0] = (int) pxToDp((float) (originalSize[0] * scale * 1.2),
                context.getResources().getDisplayMetrics());
        this.scaledSize[1] = (int) pxToDp((float) (originalSize[1] * scale * 1.2),
                context.getResources().getDisplayMetrics());
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

    public String getBuildingType() {
        return type;
    }
}
