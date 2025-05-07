package tomer.spivak.androidstudio2dgame.graphics;

import static java.lang.Math.PI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.modelEnums.CellState;

public class GraphicalBoard {
    private final PointF offset = new PointF(0,0);
    private float scale = 1f;

    private final int boardSize;
    private final Path[][] cellPaths;
    private final Point[][] cellCenters;

    private final Context context;
    private final Drawable[] originalDrawables;
    private final Bitmap[] originalsBitmaps;
    private final Bitmap[] scaledBitmaps;
    private float lastScale = -1f;

    private CellState[][] cellStates;
    private final int[] screenSize = new int[2];
    private Region[][] cellRegions;

    public GraphicalBoard(Context context, int boardSize) {
        this.context = context;
        this.boardSize = boardSize;

        cellPaths = new Path[boardSize][boardSize];
        cellCenters = new Point[boardSize][boardSize];

        int n = CellState.values().length;
        originalDrawables = new Drawable[n];
        originalsBitmaps = new Bitmap[n];
        scaledBitmaps = new Bitmap[n];

        //init grass images
        originalDrawables[0] = ContextCompat.getDrawable(context, R.drawable.grass_default);
        originalDrawables[1] = ContextCompat.getDrawable(context, R.drawable.grass_burnt);
        originalDrawables[2] = ContextCompat.getDrawable(context, R.drawable.grass_enemydeath1);
        originalDrawables[3] = ContextCompat.getDrawable(context, R.drawable.grass_enemydeath2);
        originalDrawables[4] = ContextCompat.getDrawable(context, R.drawable.grass_enemydeath3);
        originalDrawables[5] = ContextCompat.getDrawable(context, R.drawable.grass_explode);
        originalDrawables[6] = ContextCompat.getDrawable(context, R.drawable.grass_enemy_spawn_location);

        for (int i = 0; i < n; i++) {
            Bitmap bmp = Bitmap.createBitmap(originalDrawables[i].getIntrinsicWidth(), originalDrawables[i].getIntrinsicHeight(), Config.ARGB_8888);
            Canvas c = new Canvas(bmp);
            originalDrawables[i].setBounds(0, 0, c.getWidth(), c.getHeight());
            originalDrawables[i].draw(c);

            originalsBitmaps[i] = bmp;
        }

        // init cell states
        cellStates = new CellState[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                cellStates[i][j] = CellState.NORMAL;
            }
        }

        screenSize[0] = context.getResources().getDisplayMetrics().widthPixels;
        screenSize[1] = context.getResources().getDisplayMetrics().heightPixels;

        calculateCellPaths();
        buildCellRegions();
    }

    private void calculateCellPaths() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                Path path = cellPaths[i][j];
                if (path == null) {
                    path = new Path();
                    cellPaths[i][j] = path;
                } else {
                    path.reset();
                }

                float shapeStartX = (i - j) * getCellWidth() / 2f;
                float shapeStartY = (i + j) * getCellHeight() / 2f;

                float[] pts = new float[]{offset.x + shapeStartX, offset.y + shapeStartY, offset.x + shapeStartX + getCellWidth() / 2f,
                        offset.y + shapeStartY + getCellHeight() / 2f, offset.x + shapeStartX, offset.y + shapeStartY + getCellHeight(),
                        offset.x + shapeStartX - getCellWidth() / 2f, offset.y + shapeStartY + getCellHeight() / 2f};

                path.moveTo(pts[0], pts[1]);
                path.lineTo(pts[2], pts[3]);
                path.lineTo(pts[4], pts[5]);
                path.lineTo(pts[6], pts[7]);
                path.close();

                Point center = cellCenters[i][j];
                if (center == null) {
                    center = new Point();
                    cellCenters[i][j] = center;
                }
                center.set((int)(offset.x + shapeStartX), (int)(offset.y + shapeStartY + getCellHeight() / 2f));
            }
        }
    }

    //update the board's position (and dont allow the user to move the board out of bounds)
    public void updatePosition(float deltaX, float deltaY) {
        float[][] bounds = new float[2][2];
        bounds[0][0] = (boardSize / 2f) * getCellWidth() + getCellWidth() * 2;
        bounds[0][1] = screenSize[0] - (boardSize * getCellWidth()) / 2f - getCellWidth() * 2;
        bounds[1][0] = getCellHeight() * 2;
        bounds[1][1] = getCellHeight() * boardSize - screenSize[1] + getCellHeight() * 3;

        if ((deltaX > 0 && offset.x + deltaX > bounds[0][0]) || (deltaX < 0 && offset.x + deltaX < bounds[0][1])) {
            deltaX = 0;
        }
        if ((deltaY > 0 && offset.y + deltaY > bounds[1][0]) || (deltaY < 0 && offset.y + deltaY < -bounds[1][1])) {
            deltaY = 0;
        }

        if (Math.abs(deltaX) < 100 && Math.abs(deltaY) < 100) {
            offset.x += deltaX;offset.y += deltaY;
            calculateCellPaths();
            buildCellRegions();
        }
    }

    //update the scale
    public float updateScale(float scaleFactor, float focusX, float focusY) {
        float newScale = scale * scaleFactor;
        if (newScale < 0.45f || newScale > 1.2f) {
            return scale;
        }
        offset.x += (focusX - offset.x) * (1 - scaleFactor);
        offset.y += (focusY - offset.y) * (1 - scaleFactor);
        scale = newScale;
        calculateCellPaths();
        buildCellRegions();
        return newScale;
    }

    private void buildCellRegions() {
        Region clip = new Region(0, 0, screenSize[0], screenSize[1]);
        cellRegions = new Region[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                Region region = new Region();
                region.setPath(cellPaths[i][j], clip);
                cellRegions[i][j] = region;
            }
        }
    }

    public void draw(@NonNull Canvas canvas) {
        if (scale != lastScale) {
            lastScale = scale;
            for (int i = 0; i < originalsBitmaps.length; i++) {
                Drawable drawable = originalDrawables[i];
                Bitmap bitmap = originalsBitmaps[i];
                int drawableWidth = (int)(pxToDp(drawable.getIntrinsicWidth()) * 1.125f * scale);
                int drawableHeight = (int)(pxToDp(drawable.getIntrinsicHeight()) * scale);
                scaledBitmaps[i] = Bitmap.createScaledBitmap(bitmap, drawableWidth, drawableHeight, true);
            }
        }

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                Point center = cellCenters[i][j];
                if (center != null) {
                    int index;
                    if (cellStates[i][j] == null) {
                        index = 0;
                    } else {
                        index = cellStates[i][j].ordinal();
                    }
                    Bitmap bitmap = scaledBitmaps[index];
                    float x = center.x - bitmap.getWidth() * 0.5f;
                    float y = center.y - bitmap.getHeight() * 0.5f;
                    canvas.drawBitmap(bitmap, x, y, null);
                }
            }
        }
    }

    private float pxToDp(int px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public Point getSelectedCell(float clickX, float clickY) {
        Log.d("click", String.valueOf(clickX));
        if (cellRegions == null)
            return null;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (cellRegions[i][j].contains((int) clickX, (int) clickY)) {
                    return new Point(i, j);
                }
            }
        }
        return null;
    }

    public void setCellsState(CellState[][] states) {
        this.cellStates = states;
    }

    public Point[][] getCenterCells() {
        return cellCenters;
    }
    public int getCellWidth() {
        return (int)(300 * scale / Math.tan((float)(PI * 0.18)));
    }

    public int getCellHeight() {
        return (int)(300 * scale);
    }

    public float getScale() {
        return scale;
    }
}
