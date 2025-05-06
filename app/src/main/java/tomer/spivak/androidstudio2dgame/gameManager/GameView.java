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
import android.util.Log;
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
import tomer.spivak.androidstudio2dgame.gameObjects.GameObjectFactory;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.music.MusicService;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.gameObjects.GameObject;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.music.SoundEffectManager;

import android.os.Handler;
import android.os.Looper;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, TouchManager.TouchListener {
    private final GameLoop gameLoop;
    private final CustomGridView gridView;

    //custom class that handles touch events (scrolls, zoom)
    private final TouchManager touchManager;


    private final SoundEffectManager soundEffects;
    private Float scale = 1F;
    private final Bitmap morningBackground;
    private final Bitmap nightBackground;
    private Bitmap backgroundBitmap;
    long timeTillNextRound = 0;
    int currentRound = 0;
    int boardSize;
    Context context;
    private final List<GameObject> drawOrder = new ArrayList<>();
    private int shnuzes = 0;
    GameActivity gameActivity;
    private MusicService musicService;
    private final Intent musicIntent;
    private int roundsLeft = Integer.MAX_VALUE;

    private final Paint barBgPaint = new Paint();
    private final Paint barFgPaint = new Paint();
    // at the top of your class, alongside barBgPaint/barFgPaint:
    private final Paint timerPaint = new Paint();
    private final android.graphics.Rect timerBounds = new android.graphics.Rect();
    private final Paint backgroundPaint = new Paint();


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
        this.gameActivity = gameActivity;

        musicIntent = new Intent(getContext(), MusicService.class);
        context.startService(musicIntent);

        context.bindService(musicIntent, serviceConnection, Context.BIND_AUTO_CREATE);


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
        this.scale =  gridView.updateScale(scaleFactor, focusX, focusY);

        // 2) reposition and rescale every sprite under the same lock you use in draw/applyDelta
        Point[][] centers = gridView.getCenterCells();
        synchronized (drawOrder) {
            for (GameObject go : drawOrder) {
                // move it to the new scaled center
                Position p = go.getPos();
                go.setImagePoint(centers[p.getX()][p.getY()]);
                // tell it the new scale so it draws its bitmap larger/smaller
                go.setScale(scale);
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
        synchronized (drawOrder) {
            spritesSnapshot = new ArrayList<>(drawOrder);
        }

        for (GameObject gameObject : spritesSnapshot) {
            gameObject.drawView(canvas);
            drawHealthBar(gameObject, canvas, scale);
        }

        if (timeTillNextRound > 0){
            drawTimeTillNextRound(canvas, timeTillNextRound, screenWidth);
        }

        drawNumberOfRoundsLeft(canvas, roundsLeft, screenWidth);
        drawShnuzes(canvas, shnuzes, screenWidth);
    }

    public void applyDelta(List<GameObjectData> changed, List<Position> removed) {
        // grab your current transform info
        Point[][] centers = gridView.getCenterCells();

        // synchronize the whole mutation so draw() can’t run at the same time
        synchronized (drawOrder) {
            // 1) remove disappeared
            for (Position p : removed) {
                for (int i = 0; i < drawOrder.size(); i++) {
                    if (drawOrder.get(i).getPos().equals(p)) {
                        drawOrder.remove(i);
                        break;
                    }
                }
            }

            // 2) update existing or insert new
            for (GameObjectData gameObjectData : changed) {
                Position pos = new Position(gameObjectData.getX(), gameObjectData.getY());
                GameObject found = null;
                for (GameObject go : drawOrder) {
                    if (go.getPos().equals(pos)) {
                        found = go;
                        break;
                    }
                }

                if (found != null) {
                    found.updateState(gameObjectData.getState(), gameObjectData.getDirection(), gameObjectData.getHealthPercentage());
                } else {
                    Point center = centers[gameObjectData.getX()][gameObjectData.getY()];
                    GameObject go = GameObjectFactory.create(
                            context, center,
                            gameObjectData.getType(),
                            scale,
                            pos,
                            gameObjectData.getState(),
                            gameObjectData.getDirection(), gameObjectData.getHealthPercentage()
                    );
                    // insert sorted by Y
                    int idx = 0;
                    while (idx < drawOrder.size() &&
                            drawOrder.get(idx).getImagePoint().y < go.getImagePoint().y) {
                        idx++;
                    }
                    drawOrder.add(idx, go);
                }
            }
        }
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
        soundEffects.stopAllSoundEffects();


        gameLoop.stopLoop();
    }

    public MusicService getMusicService() {
        return musicService;
    }


    public void resumeGameLoop(float volumeLevel) {
        Point[][] centerCells = gridView.getCenterCells();

        synchronized (drawOrder){
            for(GameObject gameObject : drawOrder){
                gameObject.setImagePoint(centerCells[gameObject.getPos().getX()][gameObject.getPos().getY()]);
            }
        }

        if (musicService != null) {
            musicService.resumeMusic();
            musicService.setVolumeLevel(volumeLevel);
        }
        soundEffects.resumeSoundEffects();
        gameLoop.startLoop();
    }
    public void updateFromGameState(GameState gameState) {
        // swap the bitmap
        if (gameState.isDayTime()) {
            backgroundBitmap = morningBackground;
            timeTillNextRound = gameState.getTimeToNextRound();
        } else {
            backgroundBitmap = nightBackground;
            timeTillNextRound = -1;
        }
        // if you’re also tracking rounds/shnuzes here, update those too:
        currentRound = gameState.getCurrentRound();
        roundsLeft    = gameState.getNumberOfRounds() - currentRound + 1;
        shnuzes       = gameState.getShnuzes();
    }
    public void drawHealthBar(GameObject gameObject, Canvas canvas, float scale) {
        // 1) find the logical object
        float healthPer =  gameObject.getHealthPercentage();

        // 2) compute bar geometry
        int barW = (int)(gameObject.getScaledWidth() * 0.1f);
        int barH = (int)(gameObject.getScaledHeight() * 0.02f);

        Point ip = gameObject.getImagePoint();
        int x = ip.x - barW/2;
        int offset = (!gameObject.getType().equals("monster")) ? 310 : 170;
        int y = (int)(ip.y - offset * scale);

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

    public CustomGridView getGridView() {
        return gridView;
    }
}