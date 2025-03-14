package tomer.spivak.androidstudio2dgame.gameManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import tomer.spivak.androidstudio2dgame.GridView.CustomGridView;
import tomer.spivak.androidstudio2dgame.GridView.TouchHandler;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.gameObjects.GameObject;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;
import tomer.spivak.androidstudio2dgame.model.Position;


public class GameView extends SurfaceView implements SurfaceHolder.Callback,
        TouchHandler.TouchHandlerListener {

    private final GameLoop gameLoop;

    private final CustomGridView gridView;

    private final TouchHandler touchHandler;

    GameObjectManager gameObjectManager;

    private Float scale = 1F;
    private Bitmap morningBackground;
    private Bitmap nightBackground;
    private Bitmap backgroundBitmap; // This will point to one of the above.
    private Paint paint;
    long timeTillNextRound = 0;
    Paint timerPaint = new Paint();
    Rect timerBounds = new Rect();

    int boardSize;

    GameViewListener listener;

    Context context;

    public GameView(Context context, int boardSize, GameViewListener listener) {
        super(context);

        this.context = context;

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        gameLoop = new GameLoop(this, surfaceHolder, listener);
        touchHandler = new TouchHandler(context, this);
        this.boardSize = boardSize;

        gridView = new CustomGridView(context, boardSize);


        this.listener = listener;
        init();
    }

    void init(){
        morningBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background_game_morning);
        nightBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background_game_night);
        // Set the initial background (assuming morning is default)
        backgroundBitmap = morningBackground;
        paint = new Paint();
        timerPaint.setColor(Color.WHITE);
        timerPaint.setTextSize(60);
        timerPaint.setAntiAlias(true);
        timerPaint.setTextAlign(Paint.Align.LEFT);  // Align text to the left
        timerPaint.setShadowLayer(5, 0, 0, Color.BLACK);  // Add shadow for visibility
        gameObjectManager = new GameObjectManager(context, boardSize,
                gridView.getCenterCells());
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
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
        gameObjectManager.updateScaleForGameObjects(scale);
    }

    @Override
    public void onScroll(float deltaX, float deltaY) {
        gridView.updatePosition(deltaX, deltaY);
    }


    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (gameLoop != null) {
            gameLoop.stopLoop();
        }
    }


    //the user clicked on a cell, adding the selected building(if there is one) to drawn buildings
    // setting the dynamic center cell to it
    @Override
    public void onBoxClick(float x, float y) {
        Point[] cellCenterPointArray = gridView.getSelectedCell(x, y);
        if (cellCenterPointArray == null)
            return;
        Point cellPoint = cellCenterPointArray[1];
        listener.onCellClicked(cellPoint.x, cellPoint.y);
    }

    public void unpackGameState(GameState gameState) {
        updateBoard(gameState.getGrid());
        boolean timeOfDay = gameState.getTimeOfDay();
        if (timeOfDay){
            timeTillNextRound = gameState.getTimeToNextRound();
            backgroundBitmap = morningBackground;
        } else {
            timeTillNextRound = -1;
            backgroundBitmap = nightBackground;
        }

        if (gameState.getGameStatus() == GameStatus.LOST){
            Toast.makeText(context, "lost", Toast.LENGTH_SHORT).show();
            Log.d("lost", "lost");
            gameLoop.stopLoop();
        }
    }

    public void updateBoard(Cell[][] board) {
        gameObjectManager.updateGameBoardFromBoard(board, scale);
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

        //draws basic grid with grass
        gridView.setCellsState(gameObjectManager.getCellStates());
        gridView.draw(canvas);

        //draws buildings
        List<GameObject> objectsToDraw;
        ArrayList<GameObject> gameObjectsViewsArrayList = gameObjectManager
                .getGameObjectsViewsArrayList();
        synchronized (gameObjectsViewsArrayList) {
            objectsToDraw = new ArrayList<>(gameObjectsViewsArrayList);
        }
        for (GameObject gameObject : objectsToDraw) {
            if (gameObject == null)
                continue;
            drawHealthBar(gameObject, canvas);
            gameObject.drawView(canvas);
        }
        if (timeTillNextRound > 0){
            printTimeTillNextRound(canvas);
        }
    }

    private void drawHealthBar(GameObject gameObject, Canvas canvas) {
        Point pos = gameObject.getImagePoint();
        Position position = gameObject.getPos();
        float health = gameObjectManager.getBoard()[position.getX()][position.getY()].getObject()
                .getHealth();
        float maxHealth = gameObjectManager.getBoard()[position.getX()][position.getY()].getObject()
                .getMaxHealth();


        // Define health bar dimensions
        int barWidth = (int) (80 * scale);
        int barHeight = (int) (15 * scale);
        int x = pos.x - barWidth / 2;
        int y = (int) (pos.y - 90 * scale); // Position above the object

        // Background bar (gray)
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.GRAY);
        bgPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(x, y, x + barWidth, y + barHeight, bgPaint);

        // Health bar (red)
        Paint healthPaint = new Paint();
        healthPaint.setColor(Color.RED);
        healthPaint.setStyle(Paint.Style.FILL);
        int healthWidth = (int) ((health / maxHealth) * barWidth);
        canvas.drawRect(x, y, x + healthWidth, y + barHeight, healthPaint);
    }

    private void printTimeTillNextRound(Canvas canvas) {
        String timeText = "Next round: " + (timeTillNextRound / 1000) + "." +
                (timeTillNextRound % 1000) / 100 + "s";
        timerPaint.getTextBounds(timeText, 0, timeText.length(), timerBounds);
        int x = getWidth() / 2 - timerBounds.width() / 2;
        int y = 100; // Adjust as needed
        canvas.drawText(timeText, x, y, timerPaint);

    }

    public void pauseGameLoop() {
        gameLoop.stopLoop();
    }

    public void stopGameLoop() {
        gameLoop.stopLoop();
    }

    public void resumeGameLoop() {
        // Recalculate the grid centers in case the board's position has shifted.
        gameObjectManager.setCenterCells(gridView.getCenterCells());
        gameObjectManager.updateGameObjectsPositions();
        gameLoop.startLoop();
    }


}