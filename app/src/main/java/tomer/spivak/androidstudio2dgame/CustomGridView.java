package tomer.spivak.androidstudio2dgame;

import static java.lang.Math.PI;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

public class CustomGridView {
    private int numColumns, numRows;
    private float cellWidth, cellHeight, startX, startY, viewWidth, viewHeight;
    private final float angle = (float) (PI / 6);
    private Paint gridPaint;
    private Path[][] cellPaths;
    private Point[][] cellCenters;
    private float offsetX = 0;
    private float offsetY = 0;
    private final int maxCellHeight = 300;
    private final int minCellHeight = 100;


    public CustomGridView(int rows, int columns) {
        this.numRows = rows;
        this.numColumns = columns;
        init();
    }

    private void init() {
        gridPaint = new Paint();
        gridPaint.setColor(Color.GREEN);
        gridPaint.setStrokeWidth(2);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);

        cellPaths = new Path[numRows][numColumns];
        cellCenters = new Point[numRows][numColumns];

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                cellPaths[i][j] = new Path();
                cellCenters[i][j] = new Point();
            }
        }
    }
    public void updatePosition(float deltaX, float deltaY) {
        // Update the offset positions
        offsetX += deltaX;
        offsetY += deltaY;

        // Update all cell positions with the new offset
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                calculateCellPath(i, j);
            }
        }
    }

    public void updateSize(float width, float height, float pxWidth, float pxHeight) {
        this.viewWidth = width;
        this.viewHeight = height;

        // Calculate rhombus dimensions to fit view
        float rhombusWidth = width * 1;
        float rhombusHeight = height * 1;

        cellWidth = rhombusWidth / (numColumns);
        cellHeight = rhombusHeight / (numRows) * 2 - 1;

        //cellWidth = (float) (100);
        //cellHeight = (float) (100);

        float cellArea = cellHeight * cellHeight / 2;
        cellHeight = (float) sqrt(2 * cellArea*(tan(angle)));
        cellWidth = (float) (cellHeight / tan(angle));

        cellArea = cellHeight * cellWidth / 2;


        offsetX = 0;
        offsetY = 0;

        startY = (pxHeight - cellHeight * numColumns)/2 - 0;

        startX = pxWidth/2;



        // Recalculate all cell paths
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                calculateCellPath(i, j);
            }
        }
    }

    private void calculateCellPath(int row, int col) {
        Path path = cellPaths[row][col];
        path.reset();
        float romboStartX = (col - row) * cellWidth / 2;;
        float romboStartY = (col + row) * cellHeight/2;
        float adjustedStartX = startX + offsetX;
        float adjustedStartY = startY + offsetY;

        float[] points = new float[] {
                adjustedStartX + romboStartX, adjustedStartY + romboStartY,
                adjustedStartX + romboStartX + cellWidth/2, adjustedStartY + romboStartY + cellHeight/2,
                adjustedStartX + romboStartX, adjustedStartY + romboStartY + cellHeight,
                adjustedStartX + romboStartX - cellWidth/2, adjustedStartY + romboStartY + cellHeight/2

        };

        path.moveTo(points[0], points[1]);
        path.lineTo(points[2], points[3]);
        path.lineTo(points[4], points[5]);
        path.lineTo(points[6], points[7]);
        path.close();



        cellCenters[row][col].set(
                (int)(adjustedStartX + romboStartX),
                (int)(adjustedStartY + romboStartY + cellHeight/2)
        );


    }
    public void draw(Canvas canvas) {
        // Draw grid lines
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                canvas.drawPath(cellPaths[i][j], gridPaint);
            }
        }
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(30);
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                Point center = cellCenters[i][j];
                canvas.drawText(i + "," + j, center.x - 20, center.y, textPaint);
            }
        }

    }



    public void updateScale(float scaleFactor, float touchX, float touchY) {
        // Calculate the new cell dimensions based on the scale factor
        float newCellWidth = cellWidth * scaleFactor;
        float newCellHeight = cellHeight * scaleFactor;
        if (newCellHeight > maxCellHeight)
            return;
        if (newCellWidth > maxCellHeight / tan(angle))
            return;
        if (newCellWidth < minCellHeight / tan(angle))
            return;
        if (newCellHeight < minCellHeight)
            return;

        // Adjust offsets to maintain the zoom around the touch point
        float offsetChangeX = (touchX - startX - offsetX) * (1 - scaleFactor);
        float offsetChangeY = (touchY - startY - offsetY) * (1 - scaleFactor);

        // Update the offsets
        offsetX += offsetChangeX;
        offsetY += offsetChangeY;

        // Update the cell dimensions
        cellWidth = newCellWidth;
        cellHeight = newCellHeight;

        // Recalculate all cell paths with the new scale and offsets
        calculateCellPaths();
    }


    private void calculateCellPaths() {
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                calculateCellPath(i, j);
            }
        }
    }
    public Point getCellFromCoordinates(float x, float y) {
        float minDistance = Float.MAX_VALUE;
        Point result = new Point(-1, -1);

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                Point center = cellCenters[i][j];
                float distance = (float) Math.hypot(x - center.x, y - center.y);

                if (distance < minDistance) {
                    minDistance = distance;
                    result.set(i, j);
                }
            }
        }

        return result;
    }

    // Getter for cell paths (useful for drawing cell content)
    public Path getCellPath(int row, int col) {
        if (row >= 0 && row < numRows && col >= 0 && col < numColumns) {
            return cellPaths[row][col];
        }
        return null;
    }

}


