package tomer.spivak.androidstudio2dgame.GridView;

import android.content.Context;
import android.graphics.Canvas;

import android.graphics.Point;
import android.util.Log;

import androidx.core.content.ContextCompat;
import tomer.spivak.androidstudio2dgame.R;

public class DrawGridView {
    private final int numColumns;
    private final int numRows;
    //private final Paint gridPaint;
    private final GridBitmap grass;
    Context context;

    public DrawGridView(int numRows, int numColumns, Context context) {
        this.context = context;
        this.numRows = numRows;
        this.numColumns = numColumns;

       // gridPaint = new Paint();
        //gridPaint.setColor(Color.GREEN);
        //gridPaint.setStrokeWidth(2);
        //gridPaint.setStyle(Paint.Style.STROKE);
        //gridPaint.setAntiAlias(true);

        this.grass = new GridBitmap(ContextCompat.getDrawable(context, R.drawable.grass), context);



    }



    public void draw(Canvas canvas, Point[][] cellCenters, float scale) {
        grass.updateScale(scale);
        for (int i = 0; i < numRows; i++){
            for (int j = 0; j < numColumns; j++){
                float topLeftGrassX = cellCenters[i][j].x - grass.getWidth() / 2;
                float topLeftGrassY = cellCenters[i][j].y - grass.getHeight() / 2;
                canvas.drawBitmap(grass.getBitmap(), topLeftGrassX, topLeftGrassY, null);
            }
        }


        //for (int i = 0; i < numRows; i++) {
          //  for (int j = 0; j < numColumns; j++) {
                //canvas.drawPath(cellPaths[i][j], gridPaint);
            //}
        //}
        //Paint textPaint = new Paint();
        //textPaint.setColor(Color.WHITE);
        //textPaint.setTextSize(30);
        //for (int i = 0; i < numRows; i++) {
          //  for (int j = 0; j < numColumns; j++) {
            //    Point center = cellCenters[i][j];
                //canvas.drawText(i + "," + j, center.x - 20, center.y, textPaint);
            //}
        //}




    }


}
