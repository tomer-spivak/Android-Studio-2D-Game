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
import tomer.spivak.androidstudio2dgame.gameObjects.GameObjectFactory;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.modelEnums.CellState;
import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelObjects.Ruin;
import tomer.spivak.androidstudio2dgame.modelObjects.Turret;
import tomer.spivak.androidstudio2dgame.viewModel.GameViewListener;

public class GameView extends SurfaceView implements SurfaceHolder.Callback,
        TouchHandler.TouchHandlerListener {

    private final GameLoop gameLoop;

    private final CustomGridView gridView;

    private final TouchHandler touchHandler;

    private final ArrayList<GameObject> gameObjectsViewsArrayList = new ArrayList<>();

    private Float scale = 1F;
    private Bitmap morningBackground;
    private Bitmap nightBackground;
    private Bitmap backgroundBitmap; // This will point to one of the above.
    private Paint paint;
    long timeTillNextRound = 0;
    Paint timerPaint = new Paint();
    Rect timerBounds = new Rect();

    Cell[][] board;

    CellState[][] cellStates;

    Point[][] centerCells;

    int boardSize;

    GameViewListener listener;

    public GameView(Context context, int boardSize, GameViewListener listener) {
        super(context);

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

        board = new Cell[boardSize][boardSize];
        cellStates = new CellState[boardSize][boardSize];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = new Cell(new Position(i, j));
                cellStates[i][j] = CellState.NORMAL;
            }
        }

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        centerCells = gridView.getCenterCells();
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


    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (gameLoop != null) {
            gameLoop.stopLoop();
        }
    }


    //prepares the building the user picked to be placed by user with a click
    public void setSelectedBuilding(String buildingImageURL) {
        listener.onBuildingSelected(buildingImageURL.
                substring(buildingImageURL.lastIndexOf("/") + 1));

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
        setBoard(gameState.getGrid());
        boolean timeOfDay = gameState.getTimeOfDay();
        if (timeOfDay){
            timeTillNextRound = gameState.getTimeToNextRound();
            backgroundBitmap = morningBackground;
        } else {
            timeTillNextRound = -1;
            backgroundBitmap = nightBackground;
        }

        if (gameState.getGameStatus() == GameStatus.LOST){
            Toast.makeText(getContext(), "lost", Toast.LENGTH_SHORT).show();
            Log.d("lost", "lost");
            gameLoop.stopLoop();
        }
    }

    public void setBoard(Cell[][] board) {
        updateGameBoardFromBoard(board);
    }

    //takes everything in the model board into the game one
    void updateGameBoardFromBoard(Cell[][] newBoard){
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                //if both of them dont have anything, it doesnt matter to us
                if (i >= newBoard.length || j >= newBoard[0].length)
                    continue;

                Log.d("broad", newBoard.length + ", " + newBoard[0].length);
                Log.d("broad", board.length + ", " + board[0].length);
                Log.d("broad", i + ", " + j);
                if ((!newBoard[i][j].isOccupied() && !board[i][j].isOccupied())){
                    CellState cellState = newBoard[i][j].getCellState();
                    board[i][j] = new Cell(newBoard[i][j].getPosition(), cellState);
                    cellStates[i][j] = cellState;
                    continue;
                }

                 if (newBoard[i][j].isOccupied() && !board[i][j].isOccupied()){
                    Log.d("board", newBoard[i][j].getObject().toString());
                    addObjectFromModelToView(newBoard[i][j].getObject(), i, j);
                    CellState cellState = newBoard[i][j].getCellState();
                    board[i][j] = new Cell(newBoard[i][j].getPosition(), newBoard[i][j].getObject(),
                            cellState);
                     cellStates[i][j] = cellState;
                     continue;
                 }

                //if the old board has it but the new one doesnt,
                //we need to remove it.
                if (!newBoard[i][j].isOccupied() && board[i][j].isOccupied()){
                    removeGameObject(i, j);
                    CellState cellState = newBoard[i][j].getCellState();
                    board[i][j] = new Cell(newBoard[i][j].getPosition(), cellState);
                    cellStates[i][j] = cellState;
                    continue;
                }

                if (newBoard[i][j].isOccupied() && board[i][j].isOccupied()){
                    CellState cellState = newBoard[i][j].getCellState();
                    board[i][j] = new Cell(newBoard[i][j].getPosition(), newBoard[i][j].getObject()
                            , cellState);
                    cellStates[i][j] = cellState;
                    updateGameObject(newBoard[i][j], i, j);
                }
            }
        }
    }

    private void updateGameObject(Cell cell, int i, int j) {
        removeGameObject(i, j);
        addObjectFromModelToView(cell.getObject(), i, j);
    }

    private void removeGameObject(int i, int j) {
        for (int k = 0; k < gameObjectsViewsArrayList.size(); k++) {
            GameObject gameObject = gameObjectsViewsArrayList.get(k);
            if (gameObject.getPos().getX() == i && gameObject.getPos().getY() == j) {
                gameObjectsViewsArrayList.remove(k);
                break;
            }
        }
    }

    private void addObjectFromModelToView(ModelObject object, int centerX, int centerY) {
        GameObject gameObject = null;
        if (object instanceof Enemy){
            Enemy enemy = (Enemy) object;
            gameObject = GameObjectFactory.create(getContext(), centerCells[centerX][centerY],
                    enemy.getType().name().toLowerCase(), scale,
                    new Position(centerX, centerY),
                    enemy.getCurrentDirection().name().toLowerCase(), enemy.getEnemyState().
                            name().toLowerCase());
        } else if (object instanceof Ruin){
            Ruin ruin = (Ruin) object;
            gameObject = GameObjectFactory.create(getContext(),
                    centerCells[centerX][centerY], ruin.getType().name().toLowerCase(), scale,
                    new Position(centerX, centerY), ruin.getState().name().toLowerCase(), "");
        } else if (object instanceof Turret) {
            Turret turret = (Turret) object;
            gameObject = GameObjectFactory.create(getContext(),
                    centerCells[centerX][centerY], turret.getType().name().toLowerCase(), scale,
                    new Position(centerX, centerY), turret.getState().name().toLowerCase(), "");
        }
        addGameObject(gameObject);
    }

    //adds a building to the drawn buildings in order
    public void addGameObject(GameObject gameObject) {
        int i = 0;
        int size = gameObjectsViewsArrayList.size();
        while (i < size && gameObjectsViewsArrayList.get(i).getImagePoint().y <
                gameObject.getImagePoint().y) {
            i++;
        }
        gameObjectsViewsArrayList.add(i, gameObject); // Insert at the correct position
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
        gridView.setCellsState(cellStates);
        gridView.draw(canvas);

        //draws buildings
        List<GameObject> objectsToDraw;
        synchronized (gameObjectsViewsArrayList) {
            objectsToDraw = new ArrayList<>(gameObjectsViewsArrayList);
        }
        for (GameObject gameObject : objectsToDraw) {
            if (gameObject == null)
                continue;
            gameObject.drawView(canvas);
        }
        if (timeTillNextRound > 0){
            printTimeTillNextRound(canvas);
        }
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

    public Cell[][] getBoard() {
        Cell[][] original = board;
        if (original == null) {
                return null;
        }

        Cell[][] copy = new Cell[original.length][];
        for (int i = 0; i < original.length; i++) {
                if (original[i] != null) {
                    copy[i] = new Cell[original[i].length];
                    System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
                }
            }
            return copy;
    }


    public void resumeGameLoop() {
        // Recalculate the grid centers in case the board's position has shifted.
        centerCells = gridView.getCenterCells();
        updateGameObjectsPositions();
        gameLoop.startLoop();
    }

    private void updateGameObjectsPositions() {
        for (GameObject gameObject : gameObjectsViewsArrayList) {
            // Retrieve the object's current grid position
            Position pos = gameObject.getPos();
            // Update the drawing coordinate to match the newly calculated center
            gameObject.setImagePoint(centerCells[pos.getX()][pos.getY()]);
        }
    }
}