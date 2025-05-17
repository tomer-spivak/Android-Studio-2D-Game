package tomer.spivak.androidstudio2dgame.graphics;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.annotation.NonNull;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import tomer.spivak.androidstudio2dgame.GameObjectData;
import tomer.spivak.androidstudio2dgame.projectManagement.GameEventListener;
import tomer.spivak.androidstudio2dgame.logic.Cell;
import tomer.spivak.androidstudio2dgame.logic.Position;
import tomer.spivak.androidstudio2dgame.logic.modelEnums.CellState;
import tomer.spivak.androidstudio2dgame.projectManagement.MusicService;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.logic.GameState;
import tomer.spivak.androidstudio2dgame.projectManagement.SoundEffectManager;
import tomer.spivak.androidstudio2dgame.projectManagement.GameLoop;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, TouchManager.TouchListener {
    private final GameLoop gameLoop;
    //custom view class that handles the board
    private final GraphicalBoard board;
    //custom class that handles touch events (scrolls, zoom)
    private final TouchManager touchManager;

    //stores the images of the background
    private final Bitmap morningBackground;
    private final Bitmap nightBackground;
    //stores the current background
    private Bitmap backgroundBitmap;

    //saved for displaying to the user
    private long timeTillNextRound = 0;
    private int shnuzes = 0;
    private int roundsLeft = Integer.MAX_VALUE;

    private final Context context;

    //the order in which to draw the game objects on the screen (we dont anything to hide eachother that it shouldnt
    private final List<GameObject> gameObjectListDrawOrder = new ArrayList<>();

    //listener for events like clicking on a cell and game loop updating
    private final GameEventListener listener;


    //draw on the screen the health bar, the time to next round, and the background image
    private final Paint healthBarPaint = new Paint();
    private final Paint healthBarBackgroundPaint = new Paint();
    private final Paint hudPaint = new Paint();
    private final android.graphics.Rect hudBounds = new android.graphics.Rect();
    private final Paint backgroundPaint = new Paint();


    public GameView(Context context, int boardSize, GameEventListener listener) {
        super(context);
        this.context = context;
        //surface view
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        this.listener = listener;
        gameLoop = new GameLoop(this, surfaceHolder, listener);
        touchManager = new TouchManager(context, this);
        board = new GraphicalBoard(context, boardSize);

        morningBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background_game_morning);
        nightBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background_game_night);
        backgroundBitmap = morningBackground;

        healthBarBackgroundPaint.setColor(Color.GRAY);
        healthBarBackgroundPaint.setStyle(Paint.Style.FILL);
        healthBarPaint.setColor(Color.RED);
        healthBarPaint.setStyle(Paint.Style.FILL);
        hudPaint.setColor(Color.WHITE);
        hudPaint.setTextSize(48);
        hudPaint.setAntiAlias(true);
        hudPaint.setTextAlign(Paint.Align.LEFT);
        backgroundPaint.setFilterBitmap(true);

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        gameLoop.startLoop();
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

    //override the default touch event with my touch manager.
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchManager.onTouchEvent(event) || super.onTouchEvent(event);
    }

    //everytime we scale we need to update everything
    @Override
    public void onScale(float scaleFactor, float focusX, float focusY) {
        float scale = board.updateScale(scaleFactor, focusX, focusY);
        Point[][] centers = board.getCenterCells();
        synchronized (gameObjectListDrawOrder) {
            for (GameObject gameObject : gameObjectListDrawOrder) {
                Position p = gameObject.getPos();
                gameObject.setImagePoint(centers[p.getX()][p.getY()]);
                gameObject.setScale(scale);
            }
        }

    }

    //update the position of the grid view
    @Override
    public void onScroll(float deltaX, float deltaY) {
        board.updatePosition(deltaX, deltaY);
    }

    //this translates real screen coordinates to cell model coordinates
    @Override
    public void onBoxClick(float clickCoordinatesX, float clickCoordinatesY) {
        Point cellCenterPoint = board.getSelectedCell(clickCoordinatesX, clickCoordinatesY);
        if (cellCenterPoint == null)
            return;
        listener.onCellClicked(cellCenterPoint.x, cellCenterPoint.y);
    }


    @Override
    public void draw(Canvas canvas) {
        if (canvas == null)
            return;
        super.draw(canvas);
        //draw the background
        Bitmap scaledBackBitmap = Bitmap.createScaledBitmap(backgroundBitmap, getWidth(), getHeight(), true);
        canvas.drawBitmap(scaledBackBitmap, 0, 0, backgroundPaint);
        board.draw(canvas);

        List<GameObject> spritesSnapshot;
        synchronized (gameObjectListDrawOrder) {
            spritesSnapshot = new ArrayList<>(gameObjectListDrawOrder);
        }

        for (GameObject gameObject : spritesSnapshot) {
            gameObject.drawView(canvas);
            float healthPer =  gameObject.getHealthPercentage();
            int[] scaledSize = gameObject.getScaledSize();
            int healthBarWidth = (int)(scaledSize[0] * 0.2f);
            int healthBarHeight = (int)(scaledSize[1] * 0.04f);
            Point imagePoint = gameObject.getImagePoint();
            int x = imagePoint.x - healthBarWidth/2;
            int offset;
            if (!gameObject.getType().equals("monster")) {
                offset = 310;
            } else {
                offset = 170;
            }
            int y = (int)(imagePoint.y - offset * gameObject.getScale());
            canvas.drawRect(x, y, x + healthBarWidth, y + healthBarHeight, healthBarBackgroundPaint);
            int fill = (int)(healthPer * healthBarWidth);
            Log.d("health", gameObject.getType() + ", " + healthPer);
            canvas.drawRect(x, y, x + fill, y + healthBarHeight, healthBarPaint);
        }

        if (timeTillNextRound > 0){
            String timeText = "Next round: " + (timeTillNextRound / 1000) + "." + (timeTillNextRound % 1000) / 100 + "s";
            hudPaint.getTextBounds(timeText, 0, timeText.length(), hudBounds);
            int x = getWidth() / 2 - hudBounds.width() / 2 + 50;
            int y = 250;
            canvas.drawText(timeText, x, y, hudPaint);
        }

        String roundText = "number of rounds left: " + roundsLeft;
        hudPaint.getTextBounds(roundText, 0, roundText.length(), hudBounds);
        int x = getWidth() / 2 - hudBounds.width() / 2 + 50;
        int y = 100;
        canvas.drawText(roundText, x, y, hudPaint);

        //the number should be displyad in a formatted way
        String formattedShunzes = NumberFormat.getNumberInstance().format(shnuzes);

        String shunzesText = "Shnuzes: " + formattedShunzes + " \uD83D\uDCB0";

        x = getWidth() / 2 - hudBounds.width() / 2 + 100;
        y = 180;

        canvas.drawText(shunzesText, x, y, hudPaint);
    }

    public void applyRemoved(List<Position> positionsToRemove) {
        synchronized (gameObjectListDrawOrder) {
            for (Position pos : positionsToRemove) {
                for (int i = 0; i < gameObjectListDrawOrder.size(); i++) {
                    if (gameObjectListDrawOrder.get(i).getPos().equals(pos)) {
                        gameObjectListDrawOrder.remove(i);
                        break;
                    }
                }
            }
        }
    }

    /** Updates existing sprites or inserts new ones (in Y‐sorted order) */
    public void applyChanged(List<GameObjectData> changed) {
        Point[][] centers = board.getCenterCells();
        float scale = board.getScale();

        synchronized (gameObjectListDrawOrder) {
            for (GameObjectData data : changed) {
                Position objectPos = new Position(data.getX(), data.getY());
                GameObject found = null;

                // 1) try to find an existing GameObject at that position
                for (GameObject go : gameObjectListDrawOrder) {
                    if (go.getPos().equals(objectPos)) {
                        found = go;
                        break;
                    }
                }

                if (found != null) {
                    // 2a) if found, just update its state
                    found.updateState(
                            data.getState(),
                            data.getDirection(),
                            data.getHealthPercentage()
                    );
                } else {
                    // 2b) if not found, create & insert it in draw‐order
                    Point center = centers[data.getX()][data.getY()];
                    GameObject go = new GameObject(
                            context,
                            center,
                            scale,
                            objectPos,
                            data.getType(),
                            data.getState(),
                            data.getDirection(),
                            data.getHealthPercentage()
                    );

                    int insertAt = 0;
                    while (insertAt < gameObjectListDrawOrder.size() &&
                            gameObjectListDrawOrder.get(insertAt).getImagePoint().y
                                    < go.getImagePoint().y) {
                        insertAt++;
                    }
                    gameObjectListDrawOrder.add(insertAt, go);
                }
            }
        }
    }

    public void pauseGameLoop() {
        MusicService musicService = ((GameActivity)context).getMusicService();
        SoundEffectManager soundEffectsManager = ((GameActivity)context).getSoundEffectsManager();
        soundEffectsManager.pauseSoundEffects();
        if (musicService != null) {
            musicService.pauseMusic();
        }
        gameLoop.stopLoop();
    }


    public void stopGameLoop() {
        MusicService musicService = ((GameActivity)context).getMusicService();
        SoundEffectManager soundEffectsManager = ((GameActivity)context).getSoundEffectsManager();
        soundEffectsManager.stopAllSoundEffects();
        if (musicService != null) {
            SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            prefs.edit().putFloat("volume", musicService.getCurrentVolumeLevel()).apply();
            musicService.stopMusic();
        }
        gameLoop.stopLoop();
    }

    public void resumeGameLoop(float volumeLevel) {
        MusicService musicService = ((GameActivity)context).getMusicService();
        SoundEffectManager soundEffectsManager = ((GameActivity)context).getSoundEffectsManager();
        Point[][] centerCells = board.getCenterCells();
        soundEffectsManager.resumeSoundEffects();
        //setting the game object's image point in the new updated board
        synchronized (gameObjectListDrawOrder){
            for(GameObject gameObject : gameObjectListDrawOrder){
                gameObject.setImagePoint(centerCells[gameObject.getPos().getX()][gameObject.getPos().getY()]);
            }
        }
        if (musicService != null) {
            musicService.resumeMusic();
            musicService.setVolumeLevel(volumeLevel);
        }
        gameLoop.startLoop();
    }
    public void updateFromGameState(GameState gameState) {
        if (gameState.getDayTime()) {
            backgroundBitmap = morningBackground;
            timeTillNextRound = gameState.getTimeToNextRound();
        } else {
            backgroundBitmap = nightBackground;
            timeTillNextRound = 0;
        }
        int currentRound = gameState.getCurrentRound();
        roundsLeft = gameState.getNumberOfRounds() - currentRound + 1;
        shnuzes = gameState.getShnuzes();

        Cell[][] boardCells = gameState.getGrid();
        int R = boardCells.length, C = boardCells[0].length;
        CellState[][] states = new CellState[R][C];
        for (int i = 0; i < R; i++) {
            for (int j = 0; j < C; j++) {
                states[i][j] = boardCells[i][j].getCellState();
            }
        }
        board.setCellsState(states);

    }


}