package tomer.spivak.androidstudio2dgame.GridView;

import static java.lang.Math.PI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.widget.GridView;
import androidx.annotation.NonNull;

import tomer.spivak.androidstudio2dgame.modelEnums.CellState;


public class CustomGridView extends GridView {
    private final int numColumns;
    private final int numRows;

    private static final float angle = (float) (PI * 0.18);

    private final float[] currentPosition = new float[]{0,0};

    private final DrawGridView drawGridView;

    private final GridPathManager gridPathManager;

    private final GridTransformer gridTransformer;

    private CellState[][] cellStates;

    public CustomGridView(Context context, int boardSize) {
        super(context);
        this.numRows = boardSize;
        this.numColumns = boardSize;

        int baseCellHeight = 300;
        int cellWidth = (int) (baseCellHeight / Math.tan(angle));

        float startX = 0;
        float startY = 0;

        float[] startCoordinates = new float[]{startX, startY};

        drawGridView = new DrawGridView(context);

        gridPathManager = new GridPathManager(numRows, numColumns, cellWidth, baseCellHeight, startCoordinates);

        float minScale = 0.4f;
        float maxScale = 1.2f;

        gridTransformer = new GridTransformer(startX, startY, minScale, maxScale, baseCellHeight);

        gridPathManager.calculateCellPaths();

        updatePosition(-(float) 2300 /2, -(float) 1200 /2);

        cellStates = new CellState[numRows][numColumns];
        for (int i = 0; i < numRows; i++)
            for (int j = 0; j < numColumns; j++)
                cellStates[i][j] = CellState.NORMAL;
    }

    public Point[] getSelectedCell(float x, float y) {
        Path[][] cellPaths = gridPathManager.getCellPaths();
        Point[][] cellCenters = gridPathManager.getCellCenters();

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numColumns; col++) {
                Path path = cellPaths[row][col];
                if (isPointInsidePath(path, x, y)){
                    return new Point[]{cellCenters[row][col], new Point(row,col)};
                }
            }
        }
        return null;

    }

    public static boolean isPointInsidePath(Path path, float x, float y) {
        PathMeasure pathMeasure = new PathMeasure(path, true);
        float length = pathMeasure.getLength();
        float[] pos = new float[2];
        float[] vertices = new float[8];
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

    private static boolean rayIntersectsSegment(float px, float py, float x1, float y1,
                                                float x2, float y2) {
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
        int cellWidth = gridTransformer.getCellWidth();
        int cellHeight = gridTransformer.getCellHeight();
        int pxWidth = 2300;
        int pxHeight = 1200;

        float[][] bounds = new float[2][2];

        bounds[0][0] = ((float) numRows /2 * cellWidth) + cellWidth * 2;
        bounds[0][1] = pxWidth - ((float) (cellWidth * numRows) / 2) - cellWidth * 2;
        bounds[1][0] = cellHeight * 2;
        bounds[1][1] = cellHeight * numColumns - pxHeight + cellHeight * 3;

        if (deltaX > 0 && currentPosition[0] + deltaX > bounds[0][0]){
            deltaX = 0;
        }

        if (deltaX < 0 && currentPosition[0] + deltaX < bounds[0][1])
            deltaX = 0;

        if (deltaY > 0 && currentPosition[1] + deltaY > bounds[1][0]){
            deltaY = 0;
        }

        if(deltaY < 0 && currentPosition[1] + deltaY < -bounds[1][1])
            deltaY = 0;

        if (Math.abs(deltaX) < 100 && Math.abs(deltaY) < 100){
            currentPosition[0] += deltaX;
            currentPosition[1] += deltaY;
        } else {
            deltaX = 0;
            deltaY = 0;
        }
        gridTransformer.translate(deltaX, deltaY);
        gridPathManager.updateCellPaths(gridTransformer.getCurrentPosition());

    }

    public float updateScale(float scaleFactor, float focusX, float focusY) {
        float[] scale = gridTransformer.updateScale(scaleFactor, focusX, focusY);
        float scaleRatio = scale[0];
        float newScale = scale[1];

        currentPosition[0] = focusX + (currentPosition[0] - focusX) * scaleRatio;
        currentPosition[1] = focusY + (currentPosition[1] - focusY) * scaleRatio;

        gridPathManager.updateCellPaths(gridTransformer.getCurrentPosition());
        gridPathManager.setCellHeight(gridTransformer.getCellHeight());
        gridPathManager.setCellWidth(gridTransformer.getCellWidth());

        return newScale;
    }

    public Point[][] getCenterCells(){

        return gridPathManager.getCellCenters();
    }
    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        if (drawGridView != null) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    drawGridView.draw(canvas, gridPathManager.getCellCenters()[i][j], gridTransformer.getScale(), cellStates[i][j]);
                }
            }
        }
    }

    public void setCellsState(CellState[][] cellStates) {
        this.cellStates = cellStates;
    }

    public float getScale() {
        return gridTransformer.getScale();
    }
}