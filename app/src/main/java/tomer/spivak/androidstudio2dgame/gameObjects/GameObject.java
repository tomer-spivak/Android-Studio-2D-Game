package tomer.spivak.androidstudio2dgame.gameObjects;

import static androidx.core.util.TypedValueCompat.pxToDp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;


public abstract class GameObject {

    protected ImageView view;
    protected Point imagePoint;
    protected int imageUrl;
    protected String name;
    protected Context context;
    protected float scale = 1f;
    protected int[] scaledSize;
    protected int[] originalSize;
    protected Drawable drawable;
    // Optional: if you want a caption



    public GameObject(Context context, Point point, int imageUrl, String name)  {
        this.context = context;
        this.imagePoint = point;
        this.imageUrl = imageUrl;
        this.name = name;
        createView();
    }

    //creates the view of the Game Object
    private void createView() {
        ImageView imageView = new ImageView(context); // Use your Activity or Application context
        imageView.setImageResource(imageUrl);
        this.view = imageView;

        if (view.getDrawable() == null) {
            return;
        }

        drawable = view.getDrawable();

        int originalWidth = drawable.getIntrinsicWidth();
        int originalHeight = drawable.getIntrinsicHeight();
        this.originalSize = new int[] {originalWidth, originalHeight};



        scaledSize = new int[] {(int) pxToDp(originalWidth * scale * 1, context.getResources().getDisplayMetrics()),
                (int) pxToDp(originalHeight * scale * 1, context.getResources().getDisplayMetrics())};

    }

    public void setScale(float scale) {
        this.scale = scale;
        this.scaledSize[0] = (int) pxToDp(originalSize[0] * scale * 1, context.getResources().getDisplayMetrics());
        this.scaledSize[1] = (int) pxToDp(originalSize[1] * scale * 1, context.getResources().getDisplayMetrics());
    }

    public void setImagePoint(Point imagePoint) {
        this.imagePoint = imagePoint;



    }

    public Point getImagePoint() {
        return imagePoint;
    }

    public void drawView(Canvas canvas) {
        Bitmap scaledBitmap = createScaledBitmap();
        int topLeftX = imagePoint.x - (int) ((float) scaledSize[0] / 2);
        int topLeftY = imagePoint.y - scaledSize[1]/2;
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
    public abstract void update();

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



    public String getName() {
        return name;
    }
}
