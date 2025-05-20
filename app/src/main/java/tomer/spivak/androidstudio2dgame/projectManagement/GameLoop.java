package tomer.spivak.androidstudio2dgame.projectManagement;


import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

import tomer.spivak.androidstudio2dgame.graphics.GameView;

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
    private final GameEventListener listener;

    public GameLoop(GameView gameView, SurfaceHolder surfaceHolder, GameEventListener listener) {
        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
        this.listener = listener;
    }

    //function that starts the game loop
    public void startLoop() {
        //sets the run flag on
        isRunning = true;
        //starts the thread and assigns the run method below to it
        gameThread = new Thread(this);
        gameThread.start();
    }

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
                    listener.onTick(deltaTime);
                    //draws on the canvas
                    gameView.draw(canvas);
                }
            } catch (IllegalArgumentException ignored) {
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception ignored) {
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
