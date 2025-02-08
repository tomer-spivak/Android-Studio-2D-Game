package tomer.spivak.androidstudio2dgame.gameManager;


import android.graphics.Canvas;
import android.view.SurfaceHolder;

import tomer.spivak.androidstudio2dgame.viewModel.GameViewListener;

public class GameLoop implements Runnable {
    private static final double MAX_UPS = 120.0;
    private static final double UPS_PERIOD = 1E+3/MAX_UPS;
    private boolean isRunning = false;
    private final SurfaceHolder surfaceHolder;
    private final GameView gameView;
//    private double averageUPS;
//    private double averageFPS;
    private Thread gameThread;
    private final GameViewListener listener;

    public GameLoop(GameView gameView, SurfaceHolder surfaceHolder, GameViewListener listener) {
        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
        this.listener = listener;


    }

    public void startLoop() {
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        // Track the time of the previous frame.
        long previousTime = System.currentTimeMillis();

        // Game loop
        while (isRunning) {
            long currentTime = System.currentTimeMillis();
            // Calculate delta time (time passed since the last frame in milliseconds)
            long deltaTime = currentTime - previousTime;
            previousTime = currentTime;  // Update previous time for the next iteration

            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    // Pass the delta time instead of a growing elapsedTime value.
                    listener.updateGameState(deltaTime);
                    gameView.draw(canvas);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Calculate sleepTime based on your target UPS (updates per second)
            // (You might want to adjust this logic if necessary.)
            long frameTime = System.currentTimeMillis() - currentTime;
            long sleepTime = (long) (UPS_PERIOD - frameTime);
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }    public void stopLoop() {
        isRunning = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
