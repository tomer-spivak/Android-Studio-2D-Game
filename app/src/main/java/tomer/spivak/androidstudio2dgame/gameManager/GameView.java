package tomer.spivak.androidstudio2dgame.gameManager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import tomer.spivak.androidstudio2dgame.GridView.CustomGridView;
import tomer.spivak.androidstudio2dgame.GridView.TouchManager;
import tomer.spivak.androidstudio2dgame.gameObjects.GameBuilding;
import tomer.spivak.androidstudio2dgame.music.MusicService;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.gameObjects.GameObject;
import tomer.spivak.androidstudio2dgame.gameObjects.GameObjectManager;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;
import tomer.spivak.androidstudio2dgame.music.SoundEffects;

import android.os.Handler;
import android.os.Looper;

public class GameView extends SurfaceView implements SurfaceHolder.Callback,
        TouchManager.TouchListener {

    private final GameLoop gameLoop;
    private final CustomGridView gridView;
    private final TouchManager touchManager;
    private final SoundEffects soundEffects;
    GameObjectManager gameObjectManager;
    private Float scale = 1F;
    private Bitmap morningBackground;
    private Bitmap nightBackground;
    private Bitmap backgroundBitmap;
    private Paint paint;
    long timeTillNextRound = 0;
    int currentRound = 0;
    Paint timerPaint = new Paint();
    Rect timerBounds = new Rect();
    int boardSize;
    GameViewListener listener;
    Context context;

    // Reference to the MusicService and its intent.
    private MusicService musicService;
    private Intent musicIntent;

    // ServiceConnection to bind to the MusicService
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


    public GameView(Context context, int boardSize, GameViewListener listener,
                    SoundEffects soundEffects) {
        super(context);
        this.context = context;
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        gameLoop = new GameLoop(this, surfaceHolder, listener);
        touchManager = new TouchManager(context, this);
        this.boardSize = boardSize;
        gridView = new CustomGridView(context, boardSize);
        this.listener = listener;
        this.soundEffects = soundEffects;
        init();
    }

    void init(){
        morningBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background_game_morning);
        nightBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background_game_night);
        backgroundBitmap = morningBackground;
        paint = new Paint();
        timerPaint.setColor(Color.WHITE);
        timerPaint.setTextSize(60);
        timerPaint.setAntiAlias(true);
        timerPaint.setTextAlign(Paint.Align.LEFT);
        timerPaint.setShadowLayer(5, 0, 0, Color.BLACK);
        gameObjectManager = new GameObjectManager(context, boardSize, gridView.getCenterCells());

        // Start and bind the MusicService
        musicIntent = new Intent(getContext(), MusicService.class);
        context.startService(musicIntent);
        Log.d("music", "started");
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
        // Unbind the service when the view is destroyed
        context.unbindService(serviceConnection);
    }

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
            currentRound = -1;
            timeTillNextRound = gameState.getTimeToNextRound();
            backgroundBitmap = morningBackground;
        } else {
            timeTillNextRound = -1;
            currentRound = gameState.getCurrentRound();
            backgroundBitmap = nightBackground;
        }
        if (gameState.getGameStatus() == GameStatus.LOST){
            Toast.makeText(context, "lost", Toast.LENGTH_SHORT).show();
            Log.d("lost", "lost");
            stopGameLoop();
        }
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
        Bitmap scaledBackBitmap = Bitmap.createScaledBitmap(backgroundBitmap, screenWidth, screenHeight, true);
        canvas.drawBitmap(scaledBackBitmap, 0, 0, paint);
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
            drawHealthBar(gameObject, canvas);
            gameObject.drawView(canvas);
        }
        if (timeTillNextRound > 0){
            drawTimeTillNextRound(canvas);
        }
        if (currentRound > 0){
            drawRound(canvas);
        }

    }

    private void drawHealthBar(GameObject gameObject, Canvas canvas) {
        Point pos = gameObject.getImagePoint();
        tomer.spivak.androidstudio2dgame.model.Position position = gameObject.getPos();
        float health = gameObjectManager.getBoard()[position.getX()][position.getY()].getObject().getHealth();
        float maxHealth = gameObjectManager.getBoard()[position.getX()][position.getY()].getObject().getMaxHealth();
        int barWidth = (int) (80 * scale);
        int barHeight = (int) (15 * scale);
        int x = pos.x - barWidth / 2;
        int yOffset;
        if (gameObject instanceof GameBuilding){
            yOffset = 290;
        } else {
            yOffset = 170;
        }
        int y = (int) (pos.y - yOffset * scale);
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.GRAY);
        bgPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(x, y, x + barWidth, y + barHeight, bgPaint);
        Paint healthPaint = new Paint();
        healthPaint.setColor(Color.RED);
        healthPaint.setStyle(Paint.Style.FILL);
        int healthWidth = (int) ((health / maxHealth) * barWidth);
        canvas.drawRect(x, y, x + healthWidth, y + barHeight, healthPaint);
    }

    private void drawTimeTillNextRound(Canvas canvas) {
        String timeText = "Next round: " + (timeTillNextRound / 1000) + "." +
                (timeTillNextRound % 1000) / 100 + "s";
        timerPaint.getTextBounds(timeText, 0, timeText.length(), timerBounds);
        int x = getWidth() / 2 - timerBounds.width() / 2;
        int y = 100;
        canvas.drawText(timeText, x, y, timerPaint);
    }

    private void drawRound(Canvas canvas){
        String roundText = "current round: " + currentRound;
        timerPaint.getTextBounds(roundText, 0, roundText.length(), timerBounds);
        int x = getWidth() / 2 - timerBounds.width() / 2;
        int y = 100;
        canvas.drawText(roundText, x, y, timerPaint);
    }

    public void pauseGameLoop() {
        Log.d("music", String.valueOf(musicService));
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
            musicService.stopMusic();
        }

        context.stopService(musicIntent);
        soundEffects.stopSoundEffects();


        gameLoop.stopLoop();
    }

    public void resumeGameLoop() {
        gameObjectManager.setCenterCells(gridView.getCenterCells());
        gameObjectManager.updateGameObjectsPositions();
        if (musicService != null) {
            musicService.resumeMusic();
        }
        soundEffects.resumeSoundEffects();
        gameLoop.startLoop();
    }
}
