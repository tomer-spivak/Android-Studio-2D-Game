package tomer.spivak.androidstudio2dgame.GridView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Region;

import androidx.annotation.NonNull;

import tomer.spivak.androidstudio2dgame.modelEnums.CellState;


public class GraphicalBoard{
    private final int boardSize;

    private final float[] currentPosition = new float[]{0,0};

    private final DrawGridView drawGridView;

    private final GridPathManager gridPathManager;

    private final GridTransformer gridTransformer;

    private CellState[][] cellStates;

    private final int[] screenSize = new int[2];

    private static final int DEFAULT_CELL_HEIGHT = 300;
    private static final float DEFAULT_ANGLE = (float) (Math.PI * 0.18);
    private static final int MAX_TRANSLATION_DELTA = 100;
    private static final float MAX_ZOOM_IN = 1.2f;
    private static final float MAX_ZOOM_OUT = 0.45f;
    private Region[][] cellRegions;

    public GraphicalBoard(Context context, int boardSize) {
        this.boardSize = boardSize;

        int baseCellHeight = DEFAULT_CELL_HEIGHT;
        int cellWidth = (int) (baseCellHeight / Math.tan(DEFAULT_ANGLE));

        float[] startCoordinates = new float[2];

        drawGridView = new DrawGridView(context);

        gridPathManager = new GridPathManager(boardSize, cellWidth, baseCellHeight, startCoordinates);

        gridTransformer = new GridTransformer(MAX_ZOOM_OUT, MAX_ZOOM_IN, baseCellHeight);
        screenSize[0] = context.getResources().getDisplayMetrics().widthPixels;
        screenSize[1] = context.getResources().getDisplayMetrics().heightPixels;
        //gridPathManager.calculateCellPaths();
        updatePosition(-(float)  screenSize[0], -(float) screenSize[1] /2);

        cellStates = new CellState[boardSize][boardSize];
       // for (int i = 0; i < boardSize; i++)
         //   for (int j = 0; j < boardSize; j++)
           //     cellStates[i][j] = CellState.NORMAL;
    }

    public Point getSelectedCell(float clickX, float clickY) {
        if (cellRegions == null)
            return null;

        int ix = (int) clickX;
        int iy = (int) clickY;
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                Region region = cellRegions[r][c];
                if (region != null && region.contains(ix, iy)) {
                    return new Point(r, c);
                }
            }
        }
        return null;
    }
    public void updatePosition(float deltaX, float deltaY) {
        int cellWidth = gridTransformer.getCellWidth();
        int cellHeight = gridTransformer.getCellHeight();
        float[][] bounds = new float[2][2];

        bounds[0][0] = ((float) boardSize /2 * cellWidth) + cellWidth * 2;
        bounds[0][1] = screenSize[0] - ((float) (cellWidth * boardSize) / 2) - cellWidth * 2;
        bounds[1][0] = cellHeight * 2;
        bounds[1][1] = cellHeight * boardSize - screenSize[1] + cellHeight * 3;

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

        if (Math.abs(deltaX) < MAX_TRANSLATION_DELTA && Math.abs(deltaY) < MAX_TRANSLATION_DELTA){
            currentPosition[0] += deltaX;
            currentPosition[1] += deltaY;
        } else {
            deltaX = 0;
            deltaY = 0;
        }
        gridTransformer.translate(deltaX, deltaY);
        gridPathManager.updateCellPaths(gridTransformer.getCurrentPosition());
        buildCellRegions();
    }
    private void buildCellRegions() {
        Path[][] paths = gridPathManager.getCellPaths();
        Region clip = new Region(0, 0, screenSize[0], screenSize[1]);
        cellRegions = new Region[boardSize][boardSize];
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                Region region = new Region();
                region.setPath(paths[r][c], clip);
                cellRegions[r][c] = region;
            }
        }
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
        buildCellRegions();
        return newScale;
    }

    public void draw(@NonNull Canvas canvas) {
        if (drawGridView != null) {
            for (int i = 0; i < boardSize; i++) {
                for (int j = 0; j < boardSize; j++) {
                    drawGridView.draw(canvas, gridPathManager.getCellCenters()[i][j], gridTransformer.getScale(), cellStates[i][j]);
                }
            }
        }
    }

    public Point[][] getCenterCells(){
        return gridPathManager.getCellCenters();
    }

    public void setCellsState(CellState[][] cellStates) {
        this.cellStates = cellStates;
    }

    public float getScale() {
        return gridTransformer.getScale();
    }
}