package tomer.spivak.androidstudio2dgame.gameManager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.annotation.NonNull;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import tomer.spivak.androidstudio2dgame.GameObjectData;
import tomer.spivak.androidstudio2dgame.GridView.CustomGridView;
import tomer.spivak.androidstudio2dgame.GridView.TouchManager;
import tomer.spivak.androidstudio2dgame.gameActivity.GameActivity;
import tomer.spivak.androidstudio2dgame.gameActivity.GameEventListener;
import tomer.spivak.androidstudio2dgame.gameObjects.GameObjectFactory;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.CellState;
import tomer.spivak.androidstudio2dgame.music.MusicService;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.gameObjects.GameObject;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.music.SoundEffectManager;

import android.widget.LinearLayout;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, TouchManager.TouchListener {
    private final GameLoop gameLoop;
    //custom view class that handles the board
    private final CustomGridView gridView;
    //custom class that handles touch events (scrolls, zoom)
    private final TouchManager touchManager;

    //stores the images of the background
    private final Bitmap morningBackground;
    private final Bitmap nightBackground;
    //stores the current background
    private Bitmap backgroundBitmap;

    //saved for displaying to the user
    long timeTillNextRound = 0;
    private int shnuzes = 0;

    Context context;

    //the order in which to draw the game objects on the screen (we dont anything to hide eachother that it shouldnt
    private final List<GameObject> gameObjectListDrawOrder = new ArrayList<>();

    //listener for events like clicking on a cell and game loop updating
    GameEventListener listener;

    private boolean shouldResumeMusic  = false;
    private float  pendingVolumeLevel = 1f;


    private final Intent musicIntent;
    private int roundsLeft = Integer.MAX_VALUE;

    private final Paint barBgPaint = new Paint();
    private final Paint barFgPaint = new Paint();
    // at the top of your class, alongside barBgPaint/barFgPaint:
    private final Paint timerPaint = new Paint();
    private final android.graphics.Rect timerBounds = new android.graphics.Rect();
    private final Paint backgroundPaint = new Paint();


    public GameView(Context context, int boardSize, GameEventListener listener) {
        super(context);
        this.context = context;
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        gameLoop = new GameLoop(this, surfaceHolder, listener);
        touchManager = new TouchManager(context, this);
        gridView = new CustomGridView(context, boardSize);
        morningBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background_game_morning);
        nightBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background_game_night);
        backgroundBitmap = morningBackground;
        this.listener = listener;

        musicIntent = new Intent(context, MusicService.class);
        context.startForegroundService(musicIntent);
        context.startService(musicIntent);




        barBgPaint.setColor(Color.GRAY);
        barBgPaint.setStyle(Paint.Style.FILL);

        barFgPaint.setColor(Color.RED);
        barFgPaint.setStyle(Paint.Style.FILL);

        timerPaint.setColor(Color.WHITE);
        timerPaint.setTextSize(48);           // tweak size as needed
        timerPaint.setAntiAlias(true);
        timerPaint.setTextAlign(Paint.Align.LEFT);
        backgroundPaint.setFilterBitmap(true);

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
        return touchManager.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public void onScale(float scaleFactor, float focusX, float focusY) {
        float scale = gridView.updateScale(scaleFactor, focusX, focusY);
        Point[][] centers = gridView.getCenterCells();
        synchronized (gameObjectListDrawOrder) {
            for (GameObject gameObject : gameObjectListDrawOrder) {
                Position p = gameObject.getPos();
                gameObject.setImagePoint(centers[p.getX()][p.getY()]);
                gameObject.setScale(scale);
            }
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

    @Override
    public void onBoxClick(float x, float y) {
        Point[] cellCenterPointArray = gridView.getSelectedCell(x, y);
        if (cellCenterPointArray == null)
            return;
        Point cellPoint = cellCenterPointArray[1];
        listener.onCellClicked(cellPoint.x, cellPoint.y);
    }


    @Override
    public void draw(Canvas canvas) {
        if (canvas == null)
            return;
        super.draw(canvas);
        int screenWidth = getWidth();
        int screenHeight = getHeight();

        drawBackground(canvas, backgroundBitmap, screenWidth, screenHeight);
        gridView.draw(canvas);

        List<GameObject> spritesSnapshot;
        synchronized (gameObjectListDrawOrder) {
            spritesSnapshot = new ArrayList<>(gameObjectListDrawOrder);
        }

        for (GameObject gameObject : spritesSnapshot) {
            gameObject.drawView(canvas);
            drawHealthBar(gameObject, canvas);
        }

        if (timeTillNextRound > 0){
            drawTimeTillNextRound(canvas, timeTillNextRound, screenWidth);
        }

        drawNumberOfRoundsLeft(canvas, roundsLeft, screenWidth);
        drawShnuzes(canvas, shnuzes, screenWidth);
    }

    public void applyDelta(List<GameObjectData> changed, List<Position> removed) {
        Point[][] centers = gridView.getCenterCells();

        synchronized (gameObjectListDrawOrder) {
            for (Position p : removed) {
                for (int i = 0; i < gameObjectListDrawOrder.size(); i++) {
                    if (gameObjectListDrawOrder.get(i).getPos().equals(p)) {
                        gameObjectListDrawOrder.remove(i);
                        break;
                    }
                }
            }

            for (GameObjectData gameObjectData : changed) {
                Position pos = new Position(gameObjectData.getX(), gameObjectData.getY());
                GameObject found = null;
                for (GameObject go : gameObjectListDrawOrder) {
                    if (go.getPos().equals(pos)) {
                        found = go;
                        break;
                    }
                }

                if (found != null) {
                    found.updateState(gameObjectData.getState(), gameObjectData.getDirection(), gameObjectData.getHealthPercentage());
                } else {
                    Point center = centers[gameObjectData.getX()][gameObjectData.getY()];
                    GameObject go = GameObjectFactory.create(context, center, gameObjectData.getType(), gridView.getScale(), pos, gameObjectData.getState(),
                            gameObjectData.getDirection(), gameObjectData.getHealthPercentage());
                    int i = 0;
                    while (i < gameObjectListDrawOrder.size() && gameObjectListDrawOrder.get(i).getImagePoint().y < go.getImagePoint().y) {
                        i++;
                    }
                    gameObjectListDrawOrder.add(i, go);
                }
            }
        }
    }

    public void pauseGameLoop() {
        MusicService musicService = ((GameActivity)context).getMusicService();
        SoundEffectManager soundEffectsManager = ((GameActivity)context).getSoundEffectsManager();
        shouldResumeMusic = false;
        soundEffectsManager.pauseSoundEffects();
        if (musicService != null) {
            musicService.pauseMusic();
        }
        gameLoop.stopLoop();
    }


    public void stopGameLoop() {
        // grab the already‐bound service from your host Activity
        MusicService svc = ((GameActivity)context).getMusicService();
        SoundEffectManager soundEffectsManager = ((GameActivity)context).getSoundEffectsManager();
        soundEffectsManager.stopAllSoundEffects();
        if (svc != null) {
            // persist the current volume
            SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            prefs.edit()
                    .putFloat("volume", svc.getCurrentVolumeLevel())
                    .apply();
            // stop the music playback
            svc.stopMusic();
        }
        // stop your game loop as before
        gameLoop.stopLoop();
    }

    public void resumeGameLoop(float volumeLevel) {
        MusicService musicService = ((GameActivity)context).getMusicService();
        SoundEffectManager soundEffectsManager = ((GameActivity)context).getSoundEffectsManager();
        Point[][] centerCells = gridView.getCenterCells();
        soundEffectsManager.resumeSoundEffects();
        synchronized (gameObjectListDrawOrder){
            for(GameObject gameObject : gameObjectListDrawOrder){
                gameObject.setImagePoint(centerCells[gameObject.getPos().getX()][gameObject.getPos().getY()]);
            }
        }
        shouldResumeMusic = true;
        pendingVolumeLevel = volumeLevel;
        if (musicService != null) {
            shouldResumeMusic = false;
            musicService.resumeMusic();
            musicService.setVolumeLevel(pendingVolumeLevel);
        }
        gameLoop.startLoop();
    }
    public void updateFromGameState(GameState gameState) {
        // swap the bitmap
        if (gameState.isDayTime()) {
            backgroundBitmap = morningBackground;
            timeTillNextRound = gameState.getTimeToNextRound();
        } else {
            backgroundBitmap = nightBackground;
            timeTillNextRound = 0;
        }
        int currentRound = gameState.getCurrentRound();
        roundsLeft    = gameState.getNumberOfRounds() - currentRound + 1;
        shnuzes       = gameState.getShnuzes();

        Cell[][] board = gameState.getGrid();
        int R = board.length, C = board[0].length;
        CellState[][] states = new CellState[R][C];
        for (int i = 0; i < R; i++) {
            for (int j = 0; j < C; j++) {
                states[i][j] = board[i][j].getCellState();
            }
        }
        gridView.setCellsState(states);


    }
    public void drawHealthBar(GameObject gameObject, Canvas canvas) {
        // 1) find the logical object
        float healthPer =  gameObject.getHealthPercentage();

        // 2) compute bar geometry
        int barW = (int)(gameObject.getScaledWidth() * 0.1f);
        int barH = (int)(gameObject.getScaledHeight() * 0.02f);

        Point ip = gameObject.getImagePoint();
        int x = ip.x - barW/2;
        int offset = (!gameObject.getType().equals("monster")) ? 310 : 170;
        int y = (int)(ip.y - offset * gameObject.getScale());

        // 3) draw background
        canvas.drawRect(x, y, x + barW, y + barH, barBgPaint);

        // 4) draw fill
        int fill = (int)(healthPer * barW);
        canvas.drawRect(x, y, x + fill, y + barH, barFgPaint);
    }

    public void drawTimeTillNextRound(Canvas canvas, long timeTillNextRound, int screenWidth) {
        String timeText = "Next round: " + (timeTillNextRound / 1000) + "." +
                (timeTillNextRound % 1000) / 100 + "s";
        timerPaint.getTextBounds(timeText, 0, timeText.length(), timerBounds);
        int x = screenWidth / 2 - timerBounds.width() / 2 + 50;
        int y = 250;
        canvas.drawText(timeText, x, y, timerPaint);
    }

    public void drawBackground(Canvas canvas, Bitmap backgroundBitmap, int screenWidth, int screenHeight) {
        Bitmap scaledBackBitmap = Bitmap.createScaledBitmap(backgroundBitmap, screenWidth, screenHeight, true);
        canvas.drawBitmap(scaledBackBitmap, 0, 0, backgroundPaint);
    }

    public void drawShnuzes(Canvas canvas, int shunzes, int screenWidth) {
        String formattedShunzes = NumberFormat.getNumberInstance().format(shunzes);

        String shunzesText = "Shnuzes: " + formattedShunzes + " \uD83D\uDCB0";

        int x = screenWidth / 2 - timerBounds.width() / 2 + 100;
        int y = 180;

        canvas.drawText(shunzesText, x, y, timerPaint);
    }

    public void drawNumberOfRoundsLeft(Canvas canvas, int roundsLeft, int screenWidth) {
        String roundText = "number of rounds left: " + roundsLeft;
        timerPaint.getTextBounds(roundText, 0, roundText.length(), timerBounds);
        int x = screenWidth / 2 - timerBounds.width() / 2 + 50;
        int y = 100;
        canvas.drawText(roundText, x, y, timerPaint);
    }
}