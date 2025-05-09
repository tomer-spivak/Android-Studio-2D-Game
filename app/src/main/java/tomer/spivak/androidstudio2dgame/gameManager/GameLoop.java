package tomer.spivak.androidstudio2dgame.gameManager;


import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

import tomer.spivak.androidstudio2dgame.gameActivity.GameActivity;

public class GameLoop implements Runnable {

    //limits the number frames possible
    private static final double MAX_UPS = 120.0;

    //the actual millisecond each frame minimally takes
    private static final double UPS_PERIOD = 1E+3/MAX_UPS;

    //flag to control if the loop is running
    private boolean isRunning = false;

    //object that holds the canvas
    private final SurfaceHolder surfaceHolder;

    //my game view which draws on the canvas
    private final GameView gameView;

    //the game loop thread
    private Thread gameThread;

    //listener that updates the game state. its implemented in the game activity
    private final GameActivity gameActivity;

    public GameLoop(GameView gameView, SurfaceHolder surfaceHolder, GameActivity gameActivity) {
        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
        this.gameActivity = gameActivity;
    }

    //function that starts the game loop
    public void startLoop() {
        //sets the run flag on
        isRunning = true;
        //starts the thread and assigns the run method below to it
        gameThread = new Thread(this);
        gameThread.start();
    }

    //the implementation of runnable (which actually runs the game loop and uses threads)
    @Override
    public void run() {
        long previousTime = System.currentTimeMillis();
        //the actual game loop
        while (isRunning) {
            long currentTime = System.currentTimeMillis();
            //time between frames
            long deltaTime = currentTime - previousTime;
            previousTime = currentTime;

            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                //locks the canvas to only this thread and avoids any race conditions
                synchronized (surfaceHolder) {
                    //update the game state
                    gameActivity.updateGameState(deltaTime);
                    //draws on the canvas
                    gameView.draw(canvas);
                }
            } catch (IllegalArgumentException e) {
                Log.e("GameLoop", "game loop error: " + e.getMessage());
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        Log.e("GameLoop", "canvas is null: " + e.getMessage());
                    }
                }
            }
            //how long the frame took
            long frameTime = System.currentTimeMillis() - currentTime;
            //calculates the sleep time: the minimum time the frame is supposed to take minus the the time it actually took.
            long sleepTime = (long) (UPS_PERIOD - frameTime);
            //if the sleep time is positive it means the the time the frame took was less then the minimum time, hence we need to sleep
            if (sleepTime > 0) {
                try {
                    //try to avoid wasting the CPU on the thread in the sleep time
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    //function that stops the game loop
    public void stopLoop() {
        //sets the run flag off
        isRunning = false;
        try {
            //waits for the thread to finish (the run flag is off)
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
