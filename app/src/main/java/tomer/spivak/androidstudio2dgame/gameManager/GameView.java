package tomer.spivak.androidstudio2dgame.gameManager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import tomer.spivak.androidstudio2dgame.GridView.CustomGridView;
import tomer.spivak.androidstudio2dgame.GridView.TouchManager;
import tomer.spivak.androidstudio2dgame.gameActivity.GameActivity;
import tomer.spivak.androidstudio2dgame.music.MusicService;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.gameObjects.GameObject;
import tomer.spivak.androidstudio2dgame.gameObjects.GameObjectManager;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;
import tomer.spivak.androidstudio2dgame.music.SoundEffectManager;

import android.os.Handler;
import android.os.Looper;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, TouchManager.TouchListener {
    private final GameLoop gameLoop;
    private final CustomGridView gridView;

    //custom class that handles touch events (scrolls, zoom)
    private final TouchManager touchManager;


    private final SoundEffectManager soundEffects;
    GameObjectManager gameObjectManager;
    private Float scale = 1F;
    private final Bitmap morningBackground;
    private final Bitmap nightBackground;
    private Bitmap backgroundBitmap;
    long timeTillNextRound = 0;
    int currentRound = 0;
    int boardSize;
    Context context;
    GameDrawerHelper gameDrawerHelper;
    private int shnuzes = 0;
    GameActivity gameActivity;
    private MusicService musicService;
    private final Intent musicIntent;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("music", "connected");
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("music", "disconnected");
            musicService = null;
        }
    };


    public GameView(Context context, int boardSize, GameActivity gameActivity, SoundEffectManager soundEffects) {
        super(context);
        this.context = context;
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        gameLoop = new GameLoop(this, surfaceHolder, gameActivity);
        touchManager = new TouchManager(context, this);
        this.boardSize = boardSize;
        gridView = new CustomGridView(context, boardSize);
        this.soundEffects = soundEffects;
        morningBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background_game_morning);
        nightBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background_game_night);
        backgroundBitmap = morningBackground;
        gameObjectManager = new GameObjectManager(context, boardSize, gridView.getCenterCells());
        this.gameActivity = gameActivity;
        gameDrawerHelper = new GameDrawerHelper(gameObjectManager);

        musicIntent = new Intent(getContext(), MusicService.class);
        context.startService(musicIntent);

        context.bindService(musicIntent, serviceConnection, Context.BIND_AUTO_CREATE);
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
        context.unbindService(serviceConnection);
    }

    @Override
    public void onBoxClick(float x, float y) {
        Point[] cellCenterPointArray = gridView.getSelectedCell(x, y);
        if (cellCenterPointArray == null)
            return;
        Point cellPoint = cellCenterPointArray[1];
        gameActivity.onCellClicked(cellPoint.x, cellPoint.y);
    }

    public void updateBoard(Cell[][] board) {
        gameObjectManager.updateGameBoardFromBoard(board, scale);
    }

    @Override
    public void draw(Canvas canvas) {
        if (canvas == null)
            return;
        super.draw(canvas);
        int screenWidth = getWidth();
        int screenHeight = getHeight();

        gameDrawerHelper.drawBackground(canvas, backgroundBitmap, screenWidth, screenHeight);
        gridView.setCellsState(gameObjectManager.getCellStates());

        gridView.draw(canvas);
        List<GameObject> objectsToDraw;
        ArrayList<GameObject> gameObjectsViewsArrayList = gameObjectManager.getGameObjectsViewsArrayList();
        synchronized (gameObjectsViewsArrayList) {
            objectsToDraw = new ArrayList<>(gameObjectsViewsArrayList);
        }
        for (GameObject gameObject : objectsToDraw) {
            if (gameObject == null)
                continue;
            gameObject.drawView(canvas);
            gameDrawerHelper.drawHealthBar(gameObject, canvas, scale);

        }
        if (timeTillNextRound > 0){
            gameDrawerHelper.drawTimeTillNextRound(canvas, timeTillNextRound, screenWidth);
        }
        if (currentRound > 0){
            gameDrawerHelper.drawRoundNumber(canvas, currentRound, screenWidth);
        }

        gameDrawerHelper.drawShnuzes(canvas, shnuzes, screenWidth);

    }

    public void pauseGameLoop() {
        soundEffects.pauseSoundEffects();

        if (musicService != null) {
            musicService.pauseMusic();
        }
        else {
            new Handler(Looper.getMainLooper()).postDelayed(this::pauseGameLoop, 100);
        }

        gameLoop.stopLoop();
    }

    public void stopGameLoop() {
        if (musicService != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            float volume = musicService.getVolume();
            editor.putFloat("volume", volume);

            editor.apply();
            musicService.stopMusic();
        }

        context.stopService(musicIntent);
        soundEffects.stopSoundEffects();


        gameLoop.stopLoop();
    }

    public void resumeGameLoop(float volumeLevel) {
        gameObjectManager.setCenterCells(gridView.getCenterCells());
        gameObjectManager.updateGameObjectsPositions();
        if (musicService != null) {
            musicService.resumeMusic();
            musicService.setVolumeLevel(volumeLevel);
        }
        soundEffects.resumeSoundEffects();
        gameLoop.startLoop();
    }

    public MusicService getMusicService() {
        return musicService;
    }

    public void updateFromGameState(GameState gameState) {
        updateBoard(gameState.getGrid());
        boolean timeOfDay = gameState.isDayTime();
        if (timeOfDay){
            currentRound = -1;
            timeTillNextRound = gameState.getTimeToNextRound();
            backgroundBitmap = morningBackground;
        } else {
            timeTillNextRound = -1;
            currentRound = gameState.getCurrentRound();
            backgroundBitmap = nightBackground;
        }
        if (gameState.getGameStatus() == GameStatus.LOST){
            stopGameLoop();
        }
        shnuzes = gameState.getShnuzes();
    }
}