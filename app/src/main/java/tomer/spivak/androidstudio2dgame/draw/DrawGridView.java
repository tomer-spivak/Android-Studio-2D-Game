package tomer.spivak.androidstudio2dgame.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

public class DrawGridView implements Draw{
    private final int numColumns;
    private final int numRows;
    private final Paint gridPaint;

    public DrawGridView(int numRows, int numColumns) {
        this.numRows = numRows;
        this.numColumns = numColumns;

        gridPaint = new Paint();
        gridPaint.setColor(Color.GREEN);
        gridPaint.setStrokeWidth(2);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);


    }



    public void draw(Canvas canvas, Path[][] cellPaths, Point[][] cellCenters) {
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

}
