package tomer.spivak.androidstudio2dgame.GridView;

import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;
import android.view.LayoutInflater;

public class GridPathManager {
    private final int numRows, numColumns;
    private float cellWidth, cellHeight;
    private final float[] startCoordinates;
    private Path[][] cellPaths;
    private Point[][] cellCenters;
    private final float[] currentPosition = new float[]{0,0};

    public GridPathManager(int numRows, int numColumns, float cellWidth, float cellHeight, float[] startCoordinates){
        this.numRows = numRows;
        this.numColumns = numColumns;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.startCoordinates = startCoordinates;
        cellPaths = new Path[numRows][numColumns];
        cellCenters = new Point[numRows][numColumns];
    }

    private void calculateCellPath(int row, int col) {
        Path path = cellPaths[row][col];
        if (path == null) {
            path = new Path();
            cellPaths[row][col] = path;
        }
        path.reset();

        float shapeStartX = (col - row) * cellWidth / 2;;
        float shapeStartY = (col + row) * cellHeight/2;

        float adjustedStartX = startCoordinates[0] + currentPosition[0];
        float adjustedStartY = startCoordinates[1] + currentPosition[1];

        float[] points = new float[] {
                adjustedStartX + shapeStartX, adjustedStartY + shapeStartY,
                adjustedStartX + shapeStartX + cellWidth/2, adjustedStartY + shapeStartY + cellHeight/2,
                adjustedStartX + shapeStartX, adjustedStartY + shapeStartY + cellHeight,
                adjustedStartX + shapeStartX - cellWidth/2, adjustedStartY + shapeStartY + cellHeight/2

        };

        path.moveTo(points[0], points[1]);
        path.lineTo(points[2], points[3]);
        path.lineTo(points[4], points[5]);
        path.lineTo(points[6], points[7]);
        path.close();

        if (cellCenters[row][col] == null) {
            cellCenters[row][col] = new Point();
        }
        cellCenters[row][col].set(
                (int)(adjustedStartX + shapeStartX),
                (int)(adjustedStartY + shapeStartY + cellHeight/2)
        );
    }
    public void calculateCellPaths() {
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                calculateCellPath(i, j);
            }
        }
    }

    public void updateCellPaths(TransformState transformState) {
        currentPosition[0] = transformState.getPositionX();
        currentPosition[1] = transformState.getPositionY();
        calculateCellPaths();
    }

    public Path[][] getCellPaths() {
        Log.d("cell", "getCellPaths" + cellHeight + " " + cellWidth);
        return cellPaths;
    }

    public Point[][] getCellCenters() {
        return cellCenters;
    }

    public void setCellHeight(float cellHeight) {
        this.cellHeight = cellHeight;
    }

    public void setCellWidth(float cellWidth) {
        this.cellWidth = cellWidth;
    }
}
