package tomer.spivak.androidstudio2dgame.GridView;

import static java.lang.Math.PI;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;

import tomer.spivak.androidstudio2dgame.draw.DrawGridView;

public class CustomGridView {
    private final int numColumns, numRows;

    private final float[] startCoordinates;

    private final float pxDensity;

    private static final float angle = (float) (PI * 0.18);


    private final float[] currentPosition = new float[]{0,0};

    private final int maxCellHeight = 300;
    private final int minCellHeight = 150;
    private final float BaseCellHeight = 150f;

    private static CustomGridView instance;

    private final DrawGridView drawGridView;

    private final GridPathManager gridPathManager;

    private final GridTransformer gridTransformer;


    private CustomGridView(int rows, int columns, float dpHeight, float dpWidth, float density) {
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

        gridTransformer = new GridTransformer(startX, startY, 0.5f, 2f, minCellHeight, maxCellHeight);



        pxDensity = density;


        gridPathManager.calculateCellPaths();


    }
    public static CustomGridView getInstance() {
        return instance;
    }
    public static CustomGridView initInstance(int rows, int columns, float pxHeight, float pxWidth, float density) {
        instance = new CustomGridView(rows, columns, pxHeight, pxWidth, density);
        return instance;


    }



    public void updatePosition(float deltaX, float deltaY) {
        // Update the offset positions
        Log.d("updatePosition", deltaX + " " + deltaY);
        Log.d("updatePosition", currentPosition[0] + " " + currentPosition[1]);
        currentPosition[0] += deltaX;
        currentPosition[1] += deltaY;

        gridTransformer.translate(deltaX, deltaY);
        // Recalculate all cell paths
        gridPathManager.updateCellPaths(gridTransformer.getTransformState());

    }



    public void updateScale(float scaleFactor, float focusX, float focusY) {
        Log.d("updateScaling", focusX + " " + focusY);

        TransformState transformState = gridTransformer.scale(scaleFactor, focusX, focusY);

        gridPathManager.updateCellPaths(transformState);

        gridPathManager.setCellHeight(gridTransformer.getCellHeight());
        gridPathManager.setCellWidth(gridTransformer.getCellWidth());


    }



    public void draw(Canvas canvas) {

        drawGridView.draw(canvas, gridPathManager.getCellPaths(), gridPathManager.getCellCenters());
    }


}


