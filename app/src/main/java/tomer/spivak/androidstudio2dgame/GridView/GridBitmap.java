package tomer.spivak.androidstudio2dgame.GridView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class GridBitmap {

    private Bitmap bitmap;
    private final Context context;
    private final Drawable originalDrawable;
    private float scale;
    private final float widthFixer;


    public GridBitmap(Drawable drawable, Context context) {
        this.originalDrawable = drawable;
        this.context = context;
        Bitmap originalBitmap = drawableToBitmap(drawable);

        this.widthFixer = 1.09f;

        int scaledWidth = (int) (pxToDp(context, (float) drawable.getIntrinsicWidth()) * widthFixer);
        int scaledHeight = (int) pxToDp(context, (float) drawable.getIntrinsicHeight());

        this.bitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true);
        Log.d("debug", scaledWidth + ", msg " + scaledHeight);
    }

    private void updateBitmap() {

        Bitmap originalBitmap = drawableToBitmap(originalDrawable);

        int scaledWidth = (int) (pxToDp(context, (float) originalDrawable.getIntrinsicWidth()) * widthFixer * scale);
        int scaledHeight = (int) (pxToDp(context, (float) originalDrawable.getIntrinsicHeight()) * scale);

        this.bitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true);

    }

    public void updateScale(float newScale) {
        // Only update if the scale actually changed
        if (this.scale != newScale) {
            this.scale = newScale;
            updateBitmap();
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
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

    private float pxToDp(Context context, Float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public float getWidth() {
        return bitmap.getWidth();
    }

    public float getHeight() {
        return bitmap.getHeight();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
