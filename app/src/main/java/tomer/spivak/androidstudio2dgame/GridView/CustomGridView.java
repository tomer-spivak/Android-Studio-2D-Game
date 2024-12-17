package tomer.spivak.androidstudio2dgame.GridView;

import static java.lang.Math.PI;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.util.Log;
import android.widget.GridView;

import androidx.annotation.NonNull;

public class CustomGridView extends GridView {
    private int numColumns, numRows;

    private float[] startCoordinates;

    //private float pxDensity;

    private static final float angle = (float) (PI * 0.18);


    private final float[] currentPosition = new float[]{0,0};

    private final float minScale = 0.8f;
    private final float maxScale = 2f;

    //private final int minCellHeight = 150;
    private final float BaseCellHeight = 150f;

    private static CustomGridView instance;

    private DrawGridView drawGridView;

    private GridPathManager gridPathManager;

    private GridTransformer gridTransformer;


    public CustomGridView(Context context) {
        super(context);
        // Initialize custom behavior or handle XML attributes here
    }


    public void initInstance(int rows, int columns) {
        this.numRows = rows;
        this.numColumns = columns;


        float cellHeight = BaseCellHeight;
        float cellWidth = (float) (cellHeight / Math.tan(angle));




        float startX = 0;
        float startY = 0;

        this.startCoordinates = new float[] {startX, startY};



        drawGridView = new DrawGridView(numRows, numColumns);
        Log.d("cell", cellWidth + " " + cellHeight);
        gridPathManager = new GridPathManager(numRows, numColumns, cellWidth, cellHeight, startCoordinates);

        gridTransformer = new GridTransformer(startX, startY, minScale, maxScale, BaseCellHeight);



       // pxDensity = density;


        gridPathManager.calculateCellPaths();


        updatePosition(1200,-(gridTransformer.getCellHeight() * 10));
    }


    public Point getSelectedCell(float x, float y) {
        // Retrieve the current cell paths and centers
        Path[][] cellPaths = gridPathManager.getCellPaths();
        Point[][] cellCenters = gridPathManager.getCellCenters();
        // Check each cell path to see if the point is contained within it

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numColumns; col++) {
                Path path = cellPaths[row][col];
                if (isPointInsidePath(path, x, y)){
                    return cellCenters[row][col];
                }
            }
        }
        return null;

    }
    public static boolean isPointInsidePath(Path path, float x, float y) {
        PathMeasure pathMeasure = new PathMeasure(path, true);
        float length = pathMeasure.getLength();
        float[] pos = new float[2];
        float[] vertices = new float[8]; // 4 points, each with x and y

        for (int i = 0; i < 4; i++) {
            pathMeasure.getPosTan(i * length / 4, pos, null);
            vertices[i * 2] = pos[0];
            vertices[i * 2 + 1] = pos[1];
        }

        int intersections = 0;
        for (int i = 0; i < 4; i++) {
            float x1 = vertices[i * 2];
            float y1 = vertices[i * 2 + 1];
            float x2 = vertices[(i * 2 + 2) % 8];
            float y2 = vertices[(i * 2 + 3) % 8];

            if (rayIntersectsSegment(x, y, x1, y1, x2, y2)) {
                intersections++;
            }
        }

        return intersections % 2 == 1;
    }
    private static boolean rayIntersectsSegment(float px, float py, float x1, float y1, float x2, float y2) {
        if (y1 > y2) {
            float tempX = x1, tempY = y1;
            x1 = x2; y1 = y2;
            x2 = tempX; y2 = tempY;
        }

        if (py == y1 || py == y2) py += 0.1f;
        if (py < y1 || py > y2 || px >= Math.max(x1, x2)) return false;

        if (px < Math.min(x1, x2)) return true;

        float mEdge = (y2 - y1) / (x2 - x1);
        float mRay = (py - y1) / (px - x1);

        return mRay >= mEdge;
    }

    public void updatePosition(float deltaX, float deltaY) {
        // Define a maximum scroll limit (2500 * scale)
        float maxScrollX = gridTransformer.getCellWidth() * 10;
        float maxScrollY = gridTransformer.getCellHeight() * 20 - 1100;

        // Restrict horizontal movement
        if (currentPosition[0] + deltaX > maxScrollX && deltaX > 0) {
            deltaX = 0;
        }
        if (currentPosition[0] + deltaX < 0 && deltaX < 0) {
            deltaX = 0;
        }

        // Restrict vertical movement
        if (currentPosition[1] + deltaY > 0 && deltaY > 0) {
            deltaY = 0;
        }
        if (currentPosition[1] + deltaY < -maxScrollY && deltaY < 0) {
            deltaY = 0;
        }
        // Update the offset positions
        Log.d("updatePosition", deltaX + " " + deltaY);
        Log.d("updatePosition", currentPosition[0] + " " + currentPosition[1]);

        currentPosition[0] += deltaX;
        currentPosition[1] += deltaY;

        gridTransformer.translate(deltaX, deltaY);
        // Recalculate all cell paths
        gridPathManager.updateCellPaths(gridTransformer.getTransformState());

    }

    //public void fillCell(float x, float y){

    //}

    public float updateScale(float scaleFactor, float focusX, float focusY) {
        Log.d("updateScaling", focusX + " " + focusY);

        TransformState transformState = gridTransformer.scale(scaleFactor, focusX, focusY);
        gridPathManager.updateCellPaths(transformState);

        gridPathManager.setCellHeight(gridTransformer.getCellHeight());
        gridPathManager.setCellWidth(gridTransformer.getCellWidth());

        return transformState.getScale();
    }


    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        if (drawGridView != null)
            drawGridView.draw(canvas, gridPathManager.getCellPaths(), gridPathManager.getCellCenters());
    }
}


