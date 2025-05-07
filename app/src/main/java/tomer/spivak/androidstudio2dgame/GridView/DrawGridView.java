package tomer.spivak.androidstudio2dgame.GridView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.modelEnums.CellState;

public class DrawGridView {
    private final Drawable[] originalDrawables;
    private final Bitmap[] originalsBitmaps;
    private final Bitmap[] scaledBitmaps;
    private final Context context;
    private final float WIDTH_FIX = 1.125f;
    private float lastScale = -1f;

    public DrawGridView(Context context) {
        this.context = context;
        int n = CellState.values().length;
        this.originalDrawables = new Drawable[n];
        this.originalsBitmaps = new Bitmap[n];
        this.scaledBitmaps = new Bitmap[n];

        originalDrawables[0] = ContextCompat.getDrawable(context, R.drawable.grass_default);
        originalDrawables[1] = ContextCompat.getDrawable(context, R.drawable.grass_burnt);
        originalDrawables[2] = ContextCompat.getDrawable(context, R.drawable.grass_enemydeath1);
        originalDrawables[3] = ContextCompat.getDrawable(context, R.drawable.grass_enemydeath2);
        originalDrawables[4] = ContextCompat.getDrawable(context, R.drawable.grass_enemydeath3);
        originalDrawables[5] = ContextCompat.getDrawable(context, R.drawable.grass_explode);
        originalDrawables[6] = ContextCompat.getDrawable(context, R.drawable.grass_enemy_spawn_location);

        for (int i = 0; i < n; i++) {
            originalsBitmaps[i] = drawableToBitmap(originalDrawables[i]);
        }
    }

    public void draw(Canvas canvas, Point cellCenter, float scale, CellState cellState) {
        if (scale != lastScale) {
            lastScale = scale;
            for (int i = 0; i < originalsBitmaps.length; i++) {
                Drawable drawable = originalDrawables[i];
                Bitmap bitmap = originalsBitmaps[i];
                int width = (int)(pxToDp(drawable.getIntrinsicWidth()) * WIDTH_FIX * scale);
                int height = (int)(pxToDp(drawable.getIntrinsicHeight()) * scale);
                scaledBitmaps[i] = Bitmap.createScaledBitmap(bitmap, width, height, true);
            }
        }

        int index;
        if (cellState == null) {
            index = 0;
        } else {
            index = cellState.ordinal();
        }
        Bitmap bitmap = scaledBitmaps[index];

        float x = cellCenter.x - bitmap.getWidth()  * 0.5f;
        float y = cellCenter.y - bitmap.getHeight() * 0.5f;
        canvas.drawBitmap(bitmap, x, y, null);
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private float pxToDp(int px) {
        return px / context.getResources().getDisplayMetrics().density;
    }
}
