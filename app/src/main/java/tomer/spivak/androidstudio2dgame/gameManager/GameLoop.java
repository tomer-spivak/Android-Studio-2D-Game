package tomer.spivak.androidstudio2dgame.gameManager;


import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameLoop implements Runnable {
    private static final double MAX_UPS = 120.0;
    private static final double UPS_PERIOD = 1E+3/MAX_UPS;
    private boolean isRunning = false;
    private final SurfaceHolder surfaceHolder;
    private final GameView gameView;
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
        long previousTime = System.currentTimeMillis();
        while (isRunning) {
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - previousTime;
            previousTime = currentTime;

            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
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
    }
    public void stopLoop() {
        isRunning = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
