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
import tomer.spivak.androidstudio2dgame.model.GameStatus;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.model.Position;
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

    Cell[][] board;

    Point[][] centerCells;

    int boardSize;

    GameViewListener listener;

    public GameView(Context context, int boardSize, GameViewListener listener) {
        super(context);

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        gameLoop = new GameLoop(this, surfaceHolder, listener);
        touchHandler = new TouchHandler(context, this);

        gridView = new CustomGridView(context);

        this.boardSize = boardSize;

        this.listener = listener;
        init();
    }
    void init(){
        morningBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background_game_morning);
        nightBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background_game_night);
        // Set the initial background (assuming morning is default)
        backgroundBitmap = morningBackground;
        paint = new Paint();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        gridView.initInstance(boardSize, boardSize);
        centerCells = gridView.getCenterCells();
        board = new Cell[boardSize][boardSize];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = new Cell(new Position(i, j));
            }
        }
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
        Point cellPoint = cellCenterPointArray[1];
        listener.onCellClicked(cellPoint.x, cellPoint.y);

    }

    public void setBoard(Cell[][] board) {
        updateGameBoardFromBoard(board);
    }

    //in future add an actual update method because this one is just overriding

    //takes everything in the model board into the game one
    void updateGameBoardFromBoard(Cell[][] newBoard){
        for (int i = 0; i < newBoard.length; i++) {
            for (int j = 0; j < newBoard[i].length; j++) {


                //if both of them dont have anything, it doesnt matter to us
                if ((!newBoard[i][j].isOccupied() && !board[i][j].isOccupied())){
                    continue;
                }

                 if (newBoard[i][j].isOccupied() && !board[i][j].isOccupied()){
                    Log.d("board", "updated");
                    addObjectFromModelToView(newBoard[i][j].getObject(), i, j);
                    board[i][j] = new Cell(newBoard[i][j].getPosition(), newBoard[i][j].getObject());
                }

                //if the old board has it but the new one doesnt,
                //we need to remove it.
                if (!newBoard[i][j].isOccupied() && board[i][j].isOccupied()){
                    removeGameObject(i, j);
                    board[i][j] = new Cell(newBoard[i][j].getPosition());
                }

                if (newBoard[i][j].isOccupied() && board[i][j].isOccupied()){
                    board[i][j] = new Cell(newBoard[i][j].getPosition(), newBoard[i][j].getObject());
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
        String objectPath = String.valueOf(object.getClass());
        String objectType = objectPath.substring(objectPath.
                lastIndexOf('.') + 1).toLowerCase();
        GameObject gameObject;
        if (object instanceof Enemy){
            Enemy enemy = (Enemy) object;
            gameObject = GameObjectFactory.create(getContext(), centerCells[centerX][centerY],
                    objectType, scale,
                    new Position(centerX, centerY),
                    enemy.getCurrentDirection().ordinal(), enemy.getEnemyState().ordinal());
        } else
            gameObject = GameObjectFactory.create(getContext(),
                    centerCells[centerX][centerY], objectType, scale,
                    new Position(centerX, centerY), -1, -1);
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
        gridView.draw(canvas);

        //draws buildings
        List<GameObject> objectsToDraw;
        synchronized (gameObjectsViewsArrayList) {
            objectsToDraw = new ArrayList<>(gameObjectsViewsArrayList);
        }
        for (GameObject gameObject : objectsToDraw) {
            gameObject.drawView(canvas);
        }
    }

    public void unpackGameState(GameState gameState) {
        setBoard(gameState.getGrid());
        boolean timeOfDay = gameState.getTimeOfDay();
        if (timeOfDay){
            backgroundBitmap = morningBackground;
        } else {
            backgroundBitmap = nightBackground;
        }
        if (gameState.getGameStatus() == GameStatus.LOST){
            Toast.makeText(getContext(), "lost", Toast.LENGTH_SHORT).show();
            Log.d("lost", "lost");
            gameLoop.stopLoop();
        }
    }
}