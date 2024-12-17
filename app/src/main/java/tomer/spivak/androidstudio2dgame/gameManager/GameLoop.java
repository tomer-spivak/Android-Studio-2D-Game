package tomer.spivak.androidstudio2dgame.gameManager;


import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameLoop implements Runnable {
    private static final double MAX_UPS = 120.0;
    private static final double UPS_PERIOD = 1E+3/MAX_UPS;
    private boolean isRunning = false;
    private SurfaceHolder surfaceHolder;
    private GameView gameView;
    private double averageUPS;
    private double averageFPS;
    private Thread gameThread;


    public GameLoop(GameView gameView, SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
    }

    public double getAverageFPS() {
        return averageFPS;
    }

    public double getAverageUPS() {
        return averageUPS;
    }

    public void startLoop() {
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {


        //declare time and cycle count variables
        int updateCount = 0;
        int frameCount = 0;
        long startTime;
        long elapsedTime;
        long sleepTime;


        //game loop
        Canvas canvas = null;
        startTime = System.currentTimeMillis();
        while (isRunning){
            //try to update and render game
            try {
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder){
                    gameView.update();
                    gameView.draw(canvas);
                    updateCount++;


                }

                } catch (IllegalArgumentException e){
                e.printStackTrace();
            } finally {
                if (canvas != null){
                     try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                        frameCount++;
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            //pause game Loop to not exceed target UPS
            elapsedTime = System.currentTimeMillis() - startTime;
            sleepTime = (long) (updateCount*UPS_PERIOD-elapsedTime);
            if (sleepTime > 0){
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
            //skip frames to keep up with target UPS
            while (sleepTime < 0 && updateCount < MAX_UPS-1){
                gameView.update();
                updateCount++;
                elapsedTime = System.currentTimeMillis() - startTime;
                sleepTime = (long) (updateCount*UPS_PERIOD-elapsedTime);
            }
            //Calculate avg FPS and UPS
            elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime >= 1000){
                averageUPS = updateCount/(1E-3 * elapsedTime);
                averageFPS = frameCount/(1E-3 * elapsedTime);
                updateCount = 0;
                frameCount = 0;
                startTime = System.currentTimeMillis();
            }
        }
    }
}
