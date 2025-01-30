package tomer.spivak.androidstudio2dgame.gameManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Random;

import tomer.spivak.androidstudio2dgame.GridView.CustomGridView;
import tomer.spivak.androidstudio2dgame.GridView.TouchHandler;
import tomer.spivak.androidstudio2dgame.R;
//import tomer.spivak.androidstudio2dgame.gameActivity.BuildingToPick;
import tomer.spivak.androidstudio2dgame.gameObjects.GameBuilding;
import tomer.spivak.androidstudio2dgame.gameObjects.GameEnemy;
import tomer.spivak.androidstudio2dgame.gameObjects.GameObject;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, TouchHandler.TouchHandlerListener {

    private final GameLoop gameLoop;

    private final CustomGridView gridView;

    private final TouchHandler touchHandler;

    private final ArrayList<GameObject> gameObjectsViewsArrayList = new ArrayList<>();

    public GameBuilding selectedBuilding;

    private Float scale = 1F;
    private Bitmap backgroundBitmap;
    private Paint paint;

    String[][] board;

    Point[][] centerCells;

    public GameView(Context context) {
        super(context);

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        gameLoop = new GameLoop(this, surfaceHolder);

        touchHandler = new TouchHandler(context, this);

        gridView = new CustomGridView(context);

        init();
    }
    void init(){
        backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_game_morning);
        paint = new Paint();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        gridView.initInstance(10, 10);
        centerCells = gridView.getCenterCells();
        board = new String[10][10];
        gameLoop.startLoop();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        return touchHandler.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public void onScale(float scaleFactor, float focusX, float focusY) {
        scale = gridView.updateScale(scaleFactor, focusX, focusY);
        updateScaleForGameObjects();
    }

    private void updateScaleForGameObjects() {
        for (GameObject gameObject : gameObjectsViewsArrayList) {
            gameObject.setScale(scale);
        }
    }

    @Override
    public void onScroll(float deltaX, float deltaY) {
        gridView.updatePosition(deltaX, deltaY);
    }


    private boolean isCellEmpty(Point cellCenterPoint) {
        for (GameObject buildingView : gameObjectsViewsArrayList){
            if (buildingView.getImagePoint() != null && buildingView.getImagePoint().equals(cellCenterPoint)){
                return false;
            }
        }
        return true;
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }


    public void drawUPS(Canvas canvas) {
        String averageUPS = Double.toString(gameLoop.getAverageUPS());
        Paint paint = new Paint();
        int color = ContextCompat.getColor(getContext(), R.color.yellow);
        paint.setColor(color);
        paint.setTextSize(30);
        // Draw UI elements at fixed positions
        canvas.drawText("UPS: " + averageUPS, 100, 100, paint);
    }

    public void drawFPS(Canvas canvas) {
        String averageFPS = Double.toString(gameLoop.getAverageFPS());
        Paint paint = new Paint();
        int color = ContextCompat.getColor(getContext(), R.color.red);
        paint.setColor(color);
        paint.setTextSize(30);
        // Draw UI elements at fixed positions
        canvas.drawText("FPS: " + averageFPS, 100, 200, paint);
    }

    public void update() {
        // Add any update logic here
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (gameLoop != null) {
            gameLoop.stopLoop();
        }
    }


    //prepares the building the user picked to be placed by user with a click
    public void setSelectedBuilding(String buildingImageURL) {
        this.selectedBuilding = new GameBuilding(getContext(), new Point(0,0),
                buildingImageURL.
                substring(buildingImageURL.lastIndexOf("/") + 1), scale);
    }

    //the user clicked on a cell, adding the selected building(if there is one) to drawn buildings
    // setting the dynamic center cell to it
    @Override
    public void onBoxClick(float x, float y) {
        Point cellCenterPoint = gridView.getSelectedCell(x, y);
        if (selectedBuilding != null && isCellEmpty(cellCenterPoint) ) {
            selectedBuilding.setImagePoint(cellCenterPoint);
            GameObject gameObject = selectedBuilding;
            addToBoard(gameObject);
            addGameObject(selectedBuilding);
            selectedBuilding = null;
        }
    }

    public void setBoard(String[][] board) {
        this.board = board;
        updateGameBoardFromBoard();
    }

    void addToBoard(GameObject gameObject){

        for (int i = 0; i < centerCells.length; i++){
            for (int j = 0; j < centerCells.length; j++){
                Point cellPoint = centerCells[i][j];
                if (gameObject.getImagePoint().equals(cellPoint)) {
                    board[i][j] = gameObject.getImageResourceString();
                    break;
                }
            }
        }
    }

    //takes everything in the logical String[][] board into the game one
    void updateGameBoardFromBoard(){
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != null) {

                    GameObject gameObject = new GameObject(getContext(), centerCells[i][j], board[i][j], scale);
                    addGameObject(gameObject);
                    Log.d("debug", gameObject.getImageResourceString());
                }
            }
        }
    }

    //adds a building to the drawn buildings in order
    public void addGameObject(GameObject gameObject) {
        int i = 0;
        int size = gameObjectsViewsArrayList.size();
        while (i < size && gameObjectsViewsArrayList.get(i).getImagePoint().y < gameObject.getImagePoint().y) {
            i++;
        }
        Log.d("debug", String.valueOf(i));
        gameObjectsViewsArrayList.add(i, gameObject); // Insert at the correct position
        addToBoard(gameObject);
    }

    @Override
    //draw method
    public void draw(Canvas canvas) {
        if (canvas == null)
            return;
        super.draw(canvas);

        // Get the screen width and height
        int screenWidth = getWidth();
        int screenHeight = getHeight();

        Bitmap scaledBackBitmap = Bitmap.createScaledBitmap(
                backgroundBitmap,
                screenWidth,
                screenHeight,
                true
        );

        // Draws the background image
        canvas.drawBitmap(scaledBackBitmap, 0, 0, paint);

        drawFPS(canvas);
        drawUPS(canvas);

        //draws basic grid with grass
        gridView.draw(canvas);

        //draws buildings
        synchronized (gameObjectsViewsArrayList) {
            for (GameObject gameObject : gameObjectsViewsArrayList) {
                gameObject.drawView(canvas);
            }
        }
    }

    //switches to night, spawns enemies
    public void night() {
        backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_game_night);
        //spawn enemies
//        Point spawnEnemyPoint = getRandomFramePointIndex(centerCells);
//
//        GameEnemy enemy = new GameEnemy(getContext(), spawnEnemyPoint, "monster_1", scale);
//        enemy.setImagePoint(spawnEnemyPoint);
//        addGameEnemy(enemy);
//
//
//        Point spawnEnemyPoint2 = getRandomFramePointIndex(centerCells);
//
//        GameEnemy enemy2 = new GameEnemy(getContext(), spawnEnemyPoint2, "monster_2", scale);
//        enemy2.setImagePoint(spawnEnemyPoint2);
//        addGameEnemy(enemy2);
//
//
//        Point spawnEnemyPoint3 = getRandomFramePointIndex(centerCells);
//
//        GameEnemy enemy3 = new GameEnemy(getContext(), spawnEnemyPoint3, "monster_3", scale);
//        enemy3.setImagePoint(spawnEnemyPoint3);
//        addGameEnemy(enemy3);
//
//
//        Point spawnEnemyPoint4 = getRandomFramePointIndex(centerCells);
//
//        GameEnemy enemy4 = new GameEnemy(getContext(), spawnEnemyPoint4, "monster_4", scale);
//        enemy4.setImagePoint(spawnEnemyPoint4);
//        addGameEnemy(enemy4);
    }


    //adds a new enemy
    private void addGameEnemy(GameEnemy enemy) {
        //GameBuilding building = findClosestBuilding(enemy);
        //if (building != null)
          //  enemy.setAttackingBuilding(building);
        addGameObject(enemy);
    }

    //finds the building the new enemy should attack
    public GameBuilding findClosestBuilding(GameEnemy enemy) {
        GameBuilding closestBuilding = null;
        double closestDistance = Double.MAX_VALUE;

        for (GameObject building : gameObjectsViewsArrayList) {
            if (!(building instanceof GameBuilding)){
                continue;
            }
            double distance = distanceTo(enemy.getImagePoint(), building.getImagePoint());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestBuilding = (GameBuilding) building;
            }
        }

        return closestBuilding;
    }
    public double distanceTo(Point my, Point other) {
        return Math.sqrt(Math.pow(other.x - my.x, 2) + Math.pow(other.y - my.y, 2));
    }
    //gets a random point from the frame
    public Point getRandomFramePointIndex(Point[][] centerCells) {
        int rows = centerCells.length;
        int cols = centerCells[0].length;
        Random rand = new Random();

        // Total frame positions
        int leftColumnCount = rows - 2;
        int rightColumnCount = rows - 2;
        int totalFramePositions = cols + cols + leftColumnCount + rightColumnCount;

        // Generate random index from the frame positions
        int randChoice = rand.nextInt(totalFramePositions);

        if (randChoice < cols) {
            // First row
            return centerCells[0][randChoice];
        } else if (randChoice < cols + cols) {
            // Last row
            return centerCells[rows - 1][randChoice - cols];
        } else if (randChoice < cols + cols + leftColumnCount) {
            // First column (excluding first/last row)
            return centerCells[randChoice - cols - cols + 1][0];
        } else {
            // Last column (excluding first/last row)
            return centerCells[randChoice - cols - cols - leftColumnCount + 1][cols - 1];
        }
    }



    public String[][] getBoard() {
        return board;
    }
}