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

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.logic.modelEnums.CellState;
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
    private final Region[][] cellRegions;

    public GraphicalBoard(Context context, int boardSize) {
        this.context = context;
        this.boardSize = boardSize;

        cellPaths = new Path[boardSize][boardSize];
        cellCenters = new Point[boardSize][boardSize];

        originalDrawables = new Drawable[CellState.values().length];
        originalsBitmaps = new Bitmap[CellState.values().length];
        scaledBitmaps = new Bitmap[CellState.values().length];

        //init grass images
        originalDrawables[0] = ContextCompat.getDrawable(context, R.drawable.grass_default);
        originalDrawables[1] = ContextCompat.getDrawable(context, R.drawable.grass_burnt);
        originalDrawables[2] = ContextCompat.getDrawable(context, R.drawable.grass_enemydeath1);
        originalDrawables[3] = ContextCompat.getDrawable(context, R.drawable.grass_enemydeath2);
        originalDrawables[4] = ContextCompat.getDrawable(context, R.drawable.grass_enemydeath3);
        originalDrawables[5] = ContextCompat.getDrawable(context, R.drawable.grass_explode);
        originalDrawables[6] = ContextCompat.getDrawable(context, R.drawable.grass_enemy_spawn_location);

        for (int i = 0; i < CellState.values().length; i++) {
            Bitmap bitmap = Bitmap.createBitmap(originalDrawables[i].getIntrinsicWidth(), originalDrawables[i].getIntrinsicHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            originalDrawables[i].setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            originalDrawables[i].draw(canvas);

            originalsBitmaps[i] = bitmap;
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

        cellRegions = new Region[boardSize][boardSize];
        rebuildCellPathsAndRegions();
    }

    private void rebuildCellPathsAndRegions() {
        Region clip = new Region(0, 0, screenSize[0], screenSize[1]);
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
                float halfW = getCellWidth()  / 2f;
                float fullH = getCellHeight();

                path.moveTo(offset.x + shapeStartX, offset.y + shapeStartY);
                path.lineTo(offset.x + shapeStartX + halfW, offset.y + shapeStartY + fullH/2f);
                path.lineTo(offset.x + shapeStartX, offset.y + shapeStartY + fullH);
                path.lineTo(offset.x + shapeStartX - halfW, offset.y + shapeStartY + fullH/2f);
                path.close();

                Point center = cellCenters[i][j];
                if (center == null) {
                    center = new Point();
                    cellCenters[i][j] = center;
                }
                center.set((int)(offset.x + shapeStartX), (int)(offset.y + shapeStartY + fullH/2f));

                Region region = cellRegions[i][j];
                if (region == null) {
                    region = new Region();
                    cellRegions[i][j] = region;
                }
                region.setPath(path, clip);
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
            rebuildCellPathsAndRegions();
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
        rebuildCellPathsAndRegions();
        return newScale;
    }

    public void draw(@NonNull Canvas canvas) {
        if (scale != lastScale) {
            lastScale = scale;
            for (int i = 0; i < originalsBitmaps.length; i++) {
                Drawable drawable = originalDrawables[i];
                Bitmap bitmap = originalsBitmaps[i];
                int drawableWidth = (int)(drawable.getIntrinsicWidth() / context.getResources().getDisplayMetrics().density * 1.125f * scale);
                int drawableHeight = (int)(drawable.getIntrinsicHeight() / context.getResources().getDisplayMetrics().density * scale);
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

    public Point getSelectedCell(float clickX, float clickY) {
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
